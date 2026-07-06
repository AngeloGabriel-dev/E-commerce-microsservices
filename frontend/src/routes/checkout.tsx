import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { apiFetch } from "@/lib/api";
import { formatBRL, useCart } from "@/lib/cart-context";
import { useAuth } from "@/lib/auth-context";
import type {
  OrderCreateDto,
  OrderResponse,
  PaymentResponse,
} from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export const Route = createFileRoute("/checkout")({
  component: CheckoutPage,
});

function CheckoutPage() {
  const { items, subtotal, clear } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [method, setMethod] = useState("PIX");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      toast.info("Faça login para finalizar a compra");
      navigate({ to: "/login" });
    }
  }, [isAuthenticated, navigate]);

  if (items.length === 0) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <p className="text-muted-foreground">Carrinho vazio.</p>
        <Button asChild className="mt-4">
          <Link to="/">Voltar</Link>
        </Button>
      </div>
    );
  }

  async function submit() {
    setLoading(true);
    try {
      const dto: OrderCreateDto = {
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
        })),
      };
      const order = await apiFetch<OrderResponse>("/api/v1/orders", {
        method: "POST",
        body: JSON.stringify(dto),
      });
      const payment = await apiFetch<PaymentResponse>(
        `/api/v1/payments/order/${order.id}`,
        {
          method: "POST",
          body: JSON.stringify({ paymentMethod: method }),
        },
      );
      clear();
      toast.success("Pedido criado com sucesso!");
      const url = payment.mpInitPoint ?? payment.mpSandboxInitPoint;
      if (url) {
        window.location.href = url;
      } else {
        navigate({ to: "/orders" });
      }
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Falha no checkout");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <h1 className="mb-6 text-2xl font-semibold tracking-tight">Checkout</h1>
      <div className="space-y-4 rounded-xl border border-border/60 bg-card p-6">
        <h2 className="font-medium">Resumo do pedido</h2>
        {items.map((i) => (
          <div key={i.productId} className="flex justify-between text-sm">
            <span className="text-muted-foreground">
              {i.quantity}× {i.name}
            </span>
            <span>{formatBRL(i.price * i.quantity)}</span>
          </div>
        ))}
        <div className="flex justify-between border-t border-border pt-4 text-lg font-semibold">
          <span>Total</span>
          <span className="text-gradient-gold">{formatBRL(subtotal)}</span>
        </div>
      </div>

      <div className="mt-6 space-y-2 rounded-xl border border-border/60 bg-card p-6">
        <Label>Método de pagamento</Label>
        <Select value={method} onValueChange={setMethod}>
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="PIX">PIX</SelectItem>
            <SelectItem value="CREDIT_CARD">Cartão de crédito</SelectItem>
            <SelectItem value="DEBIT_CARD">Cartão de débito</SelectItem>
            <SelectItem value="BOLETO">Boleto</SelectItem>
          </SelectContent>
        </Select>
        <p className="pt-2 text-xs text-muted-foreground">
          Você será redirecionado para o Mercado Pago para concluir o pagamento.
        </p>
      </div>

      <Button size="lg" className="mt-6 w-full" onClick={submit} disabled={loading}>
        {loading ? "Processando..." : `Pagar ${formatBRL(subtotal)}`}
      </Button>
    </div>
  );
}