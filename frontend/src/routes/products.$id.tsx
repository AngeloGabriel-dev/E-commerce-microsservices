import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api";
import type { Product } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { formatBRL, useCart } from "@/lib/cart-context";
import { toast } from "sonner";
import { ShoppingBag, ArrowLeft } from "lucide-react";

export const Route = createFileRoute("/products/$id")({
  component: ProductPage,
});

function ProductPage() {
  const { id } = Route.useParams();
  const { add } = useCart();
  const { data, isLoading, error } = useQuery({
    queryKey: ["product", id],
    queryFn: () =>
      apiFetch<Product>(`/api/v1/products/${id}`, { auth: false }),
  });

  if (isLoading) {
    return (
      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 md:grid-cols-2">
        <Skeleton className="aspect-square rounded-2xl" />
        <div className="space-y-4">
          <Skeleton className="h-8 w-2/3" />
          <Skeleton className="h-4 w-1/3" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-10 w-1/2" />
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center">
        <p className="text-muted-foreground">Produto não encontrado.</p>
        <Button asChild variant="outline" className="mt-4">
          <Link to="/">
            <ArrowLeft className="mr-1 h-4 w-4" /> Voltar
          </Link>
        </Button>
      </div>
    );
  }

  const image = data.imageUrls?.[0];

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <Link
        to="/"
        className="mb-6 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" /> Voltar
      </Link>
      <div className="grid gap-10 md:grid-cols-2">
        <div className="overflow-hidden rounded-2xl border border-border/60 bg-card">
          {image ? (
            <img
              src={image}
              alt={data.name}
              className="aspect-square w-full object-cover"
            />
          ) : (
            <div className="grid aspect-square place-items-center bg-gradient-to-br from-muted to-secondary text-6xl font-black text-muted-foreground/40">
              {data.name.charAt(0).toUpperCase()}
            </div>
          )}
          {data.imageUrls && data.imageUrls.length > 1 && (
            <div className="flex gap-2 border-t border-border p-2">
              {data.imageUrls.slice(1, 5).map((u) => (
                <img
                  key={u}
                  src={u}
                  alt=""
                  className="h-16 w-16 rounded-md object-cover"
                />
              ))}
            </div>
          )}
        </div>
        <div>
          <span className="text-xs uppercase tracking-wider text-muted-foreground">
            {data.category}
          </span>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight">
            {data.name}
          </h1>
          <div className="mt-4 text-3xl font-bold text-gradient-gold">
            {formatBRL(data.price)}
          </div>
          <p className="mt-2 text-sm text-muted-foreground">
            SKU: {data.sku} · {data.stock} em estoque
          </p>
          {data.description && (
            <p className="mt-6 whitespace-pre-line leading-relaxed text-foreground/90">
              {data.description}
            </p>
          )}
          <Button
            size="lg"
            className="mt-8 w-full md:w-auto"
            disabled={data.stock === 0}
            onClick={() => {
              add(data);
              toast.success("Adicionado ao carrinho");
            }}
          >
            <ShoppingBag className="mr-2 h-4 w-4" />
            {data.stock === 0 ? "Esgotado" : "Adicionar ao carrinho"}
          </Button>
        </div>
      </div>
    </div>
  );
}