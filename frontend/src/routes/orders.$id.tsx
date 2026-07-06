import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";
import type { OrderResponse } from "@/lib/types";
import { formatBRL } from "@/lib/cart-context";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";

export const Route = createFileRoute("/orders/$id")({
  component: OrderDetailPage,
});

function OrderDetailPage() {
  const { id } = Route.useParams();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) navigate({ to: "/login" });
  }, [isAuthenticated, navigate]);

  const { data, isLoading } = useQuery({
    queryKey: ["order", id],
    queryFn: () => apiFetch<OrderResponse>(`/api/v1/orders/${id}`),
    enabled: isAuthenticated,
  });

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <Link
        to="/orders"
        className="mb-6 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" /> Meus pedidos
      </Link>
      {isLoading && <Skeleton className="h-64 w-full rounded-xl" />}
      {data && (
        <div className="rounded-xl border border-border/60 bg-card p-6">
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-xl font-semibold">
                Pedido #{data.id.slice(0, 8)}
              </h1>
              <Badge variant="outline" className="mt-2">
                {data.status}
              </Badge>
            </div>
            <div className="text-right">
              <div className="text-xs text-muted-foreground">Total</div>
              <div className="text-2xl font-bold text-gradient-gold">
                {formatBRL(data.totalPrice)}
              </div>
            </div>
          </div>
          <div className="mt-6 space-y-4">
            {data.sellerOrders?.map((so) => (
              <div
                key={so.id}
                className="rounded-lg border border-border bg-background/40 p-4"
              >
                <div className="mb-3 flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">
                    Vendedor {so.sellerId.slice(0, 8)}
                  </span>
                  <Badge>{so.status}</Badge>
                </div>
                <div className="space-y-1 text-sm">
                  {so.items.map((it) => (
                    <div key={it.id} className="flex justify-between">
                      <span>
                        {it.quantity}× {it.productName}
                      </span>
                      <span>{formatBRL(it.unitPrice * it.quantity)}</span>
                    </div>
                  ))}
                </div>
                <div className="mt-2 flex justify-between border-t border-border pt-2 text-sm font-medium">
                  <span>Subtotal</span>
                  <span>{formatBRL(so.subTotal)}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}