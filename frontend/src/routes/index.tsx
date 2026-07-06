import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api";
import type { PageResponse, Product } from "@/lib/types";
import { ProductCard } from "@/components/product-card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ArrowRight, Sparkles } from "lucide-react";

export const Route = createFileRoute("/")({
  component: Home,
});

function Home() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["products", { page: 0, size: 12 }],
    queryFn: () =>
      apiFetch<PageResponse<Product>>("/api/v1/products?page=0&size=12", {
        auth: false,
      }),
  });

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      {/* Hero */}
      <section className="relative mb-12 overflow-hidden rounded-2xl border border-border/60 bg-gradient-to-br from-card to-background px-8 py-16 md:px-14 md:py-20">
        <div className="absolute -right-16 -top-16 h-64 w-64 rounded-full bg-primary/20 blur-3xl" />
        <div className="absolute -bottom-24 -left-16 h-64 w-64 rounded-full bg-primary/10 blur-3xl" />
        <div className="relative max-w-2xl">
          <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
            <Sparkles className="h-3 w-3" />
            Marketplace premium
          </span>
          <h1 className="mt-4 text-4xl font-bold tracking-tight md:text-6xl">
            Descubra produtos com <span className="text-gradient-gold">acabamento premium</span>
          </h1>
          <p className="mt-4 text-lg text-muted-foreground">
            Uma curadoria de itens de vendedores verificados. Pagamento seguro
            via Mercado Pago.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Button asChild size="lg">
              <a href="#produtos">
                Explorar produtos <ArrowRight className="ml-1 h-4 w-4" />
              </a>
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/register">Criar conta</Link>
            </Button>
          </div>
        </div>
      </section>

      <section id="produtos">
        <div className="mb-6 flex items-end justify-between">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight">Em destaque</h2>
            <p className="text-sm text-muted-foreground">Produtos ativos no catálogo</p>
          </div>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-6 text-sm text-destructive-foreground">
            Não foi possível carregar os produtos. Verifique se o gateway está
            acessível em <code>{import.meta.env.VITE_API_BASE_URL}</code>.
          </div>
        )}

        {isLoading && (
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-72 w-full rounded-xl" />
            ))}
          </div>
        )}

        {data && data.content.length === 0 && (
          <div className="rounded-lg border border-border bg-card p-10 text-center text-muted-foreground">
            Nenhum produto cadastrado ainda.
          </div>
        )}

        {data && data.content.length > 0 && (
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            {data.content.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
