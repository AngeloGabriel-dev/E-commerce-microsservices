import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";
import type { OrderResponse, PageResponse } from "@/lib/types";
import { formatBRL } from "@/lib/cart-context";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export const Route = createFileRoute("/orders")({
  component: OrdersPage,
});

function OrdersPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) navigate({ to: "/login" });
  }, [isAuthenticated, navigate]);

  const { data, isLoading } = useQuery({
    queryKey: ["my-orders"],
    queryFn: () =>
      apiFetch<PageResponse<OrderResponse>>(
        "/api/v1/orders/my-orders?page=0&size=20",
      ),
    enabled: isAuthenticated,
  });

  return (
    <div className="mx-auto max-w-4xl px-4 py-10">
      <h1 className="mb-6 text-2xl font-semibold tracking-tight">Meus pedidos</h1>
      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}
        </div>
      )}
      {data && data.content.length === 0 && (
        <div className="rounded-lg border border-border bg-card p-10 text-center text-muted-foreground">
          Nenhum pedido ainda.{" "}
          <Link to="/" className="text-primary hover:underline">
            Comece agora
          </Link>
        </div>
      )}
      <div className="space-y-3">
        {data?.content.map((order) => (
          <div
            key={order.id}
            className="rounded-xl border border-border/60 bg-card p-5"
          >
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <div className="text-xs text-muted-foreground">
                  Pedido #{order.id.slice(0, 8)}
                </div>
                <div className="mt-1 flex items-center gap-2">
                  <Badge variant="outline">{order.status}</Badge>
                  {order.createdAt && (
                    <span className="text-xs text-muted-foreground">
                      {new Date(order.createdAt).toLocaleString("pt-BR")}
                    </span>
                  )}
                </div>
              </div>
              <div className="text-right">
                <div className="text-xs text-muted-foreground">Total</div>
                <div className="text-lg font-semibold text-gradient-gold">
                  {formatBRL(order.totalPrice)}
                </div>
              </div>
            </div>
            <div className="mt-4 space-y-1 border-t border-border pt-3 text-sm">
              {order.sellerOrders?.flatMap((so) =>
                so.items.map((it) => (
                  <div key={it.id} className="flex justify-between text-muted-foreground">
                    <span>
                      {it.quantity}× {it.productName}
                    </span>
                    <span>{formatBRL(it.unitPrice * it.quantity)}</span>
                  </div>
                )),
              )}
            </div>
            <Button asChild variant="link" size="sm" className="mt-2 px-0">
              <Link to="/orders/$id" params={{ id: order.id }}>
                Ver detalhes
              </Link>
            </Button>
          </div>
        ))}
      </div>
    </div>
  );
}