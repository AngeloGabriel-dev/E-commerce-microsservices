import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect, useState, type FormEvent } from "react";
import { toast } from "sonner";
import { apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";
import type { ProductCreateDto, Product } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

export const Route = createFileRoute("/seller")({
  component: SellerPage,
});

function SellerPage() {
  const { hasRole, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState<ProductCreateDto>({
    name: "",
    description: "",
    price: 0,
    stock: 0,
    category: "",
    sku: "",
    imageUrls: [],
  });
  const [imagesText, setImagesText] = useState("");

  useEffect(() => {
    if (!isAuthenticated) navigate({ to: "/login" });
    else if (!hasRole("ROLE_SELLER")) {
      toast.error("Acesso restrito a vendedores");
      navigate({ to: "/" });
    }
  }, [isAuthenticated, hasRole, navigate]);

  const set = <K extends keyof ProductCreateDto>(
    k: K,
    v: ProductCreateDto[K],
  ) => setForm((f) => ({ ...f, [k]: v }));

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const dto: ProductCreateDto = {
        ...form,
        imageUrls: imagesText
          .split("\n")
          .map((s) => s.trim())
          .filter(Boolean),
      };
      const created = await apiFetch<Product>("/api/v1/products", {
        method: "POST",
        body: JSON.stringify(dto),
      });
      toast.success("Produto criado!");
      navigate({ to: "/products/$id", params: { id: created.id } });
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Falha ao criar");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <h1 className="mb-1 text-2xl font-semibold tracking-tight">Novo produto</h1>
      <p className="mb-6 text-sm text-muted-foreground">
        Cadastre um item no seu catálogo.
      </p>
      <form
        onSubmit={onSubmit}
        className="space-y-4 rounded-xl border border-border/60 bg-card p-6"
      >
        <div className="space-y-1.5">
          <Label htmlFor="name">Nome</Label>
          <Input
            id="name"
            value={form.name}
            onChange={(e) => set("name", e.target.value)}
            minLength={2}
            maxLength={200}
            required
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="description">Descrição</Label>
          <Textarea
            id="description"
            value={form.description ?? ""}
            onChange={(e) => set("description", e.target.value)}
            rows={4}
          />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1.5">
            <Label htmlFor="price">Preço (R$)</Label>
            <Input
              id="price"
              type="number"
              step="0.01"
              min="0.01"
              value={form.price || ""}
              onChange={(e) => set("price", Number(e.target.value))}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="stock">Estoque</Label>
            <Input
              id="stock"
              type="number"
              min="0"
              value={form.stock}
              onChange={(e) => set("stock", Number(e.target.value))}
              required
            />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1.5">
            <Label htmlFor="category">Categoria</Label>
            <Input
              id="category"
              value={form.category}
              onChange={(e) => set("category", e.target.value)}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="sku">SKU</Label>
            <Input
              id="sku"
              value={form.sku}
              onChange={(e) => set("sku", e.target.value)}
              minLength={3}
              maxLength={50}
              required
            />
          </div>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="images">URLs de imagens (uma por linha)</Label>
          <Textarea
            id="images"
            value={imagesText}
            onChange={(e) => setImagesText(e.target.value)}
            rows={3}
            placeholder="https://..."
          />
        </div>
        <Button type="submit" className="w-full" disabled={loading}>
          {loading ? "Criando..." : "Criar produto"}
        </Button>
      </form>
    </div>
  );
}