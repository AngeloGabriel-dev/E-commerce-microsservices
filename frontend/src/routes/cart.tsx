import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Trash2, ShoppingBag } from "lucide-react";
import { formatBRL, useCart } from "@/lib/cart-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export const Route = createFileRoute("/cart")({
  component: CartPage,
});

function CartPage() {
  const { items, subtotal, setQuantity, remove } = useCart();
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div className="mx-auto grid min-h-[60vh] max-w-md place-items-center px-4 text-center">
        <div>
          <ShoppingBag className="mx-auto h-12 w-12 text-muted-foreground" />
          <h1 className="mt-4 text-xl font-semibold">Seu carrinho está vazio</h1>
          <Button asChild className="mt-4">
            <Link to="/">Explorar produtos</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="mb-6 text-2xl font-semibold tracking-tight">Carrinho</h1>
      <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
        <div className="space-y-3">
          {items.map((item) => (
            <div
              key={item.productId}
              className="flex items-center gap-4 rounded-xl border border-border/60 bg-card p-4"
            >
              <div className="h-20 w-20 shrink-0 overflow-hidden rounded-md bg-muted">
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.name}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="grid h-full w-full place-items-center text-xl font-bold text-muted-foreground/40">
                    {item.name.charAt(0)}
                  </div>
                )}
              </div>
              <div className="min-w-0 flex-1">
                <Link
                  to="/products/$id"
                  params={{ id: item.productId }}
                  className="line-clamp-2 font-medium hover:text-primary"
                >
                  {item.name}
                </Link>
                <div className="mt-1 text-sm text-muted-foreground">
                  {formatBRL(item.price)}
                </div>
              </div>
              <Input
                type="number"
                min={1}
                max={item.stock}
                value={item.quantity}
                onChange={(e) =>
                  setQuantity(item.productId, Number(e.target.value) || 1)
                }
                className="w-20"
              />
              <div className="w-24 text-right font-semibold">
                {formatBRL(item.price * item.quantity)}
              </div>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => remove(item.productId)}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          ))}
        </div>
        <aside className="h-fit rounded-xl border border-border/60 bg-card p-6">
          <h2 className="text-lg font-semibold">Resumo</h2>
          <div className="mt-4 flex justify-between text-sm text-muted-foreground">
            <span>Subtotal</span>
            <span>{formatBRL(subtotal)}</span>
          </div>
          <div className="mt-2 flex justify-between border-t border-border pt-4 text-lg font-semibold">
            <span>Total</span>
            <span className="text-gradient-gold">{formatBRL(subtotal)}</span>
          </div>
          <Button
            size="lg"
            className="mt-6 w-full"
            onClick={() => navigate({ to: "/checkout" })}
          >
            Finalizar compra
          </Button>
        </aside>
      </div>
    </div>
  );
}