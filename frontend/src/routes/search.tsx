import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api";
import type { PageResponse, Product } from "@/lib/types";
import { ProductCard } from "@/components/product-card";
import { Skeleton } from "@/components/ui/skeleton";
import { z } from "zod";

const searchSchema = z.object({
  q: z.string().catch(""),
});

export const Route = createFileRoute("/search")({
  validateSearch: searchSchema,
  component: SearchPage,
});

function SearchPage() {
  const { q } = Route.useSearch();
  const { data, isLoading } = useQuery({
    queryKey: ["search", q],
    queryFn: () =>
      apiFetch<PageResponse<Product>>(
        `/api/v1/products/search?q=${encodeURIComponent(q)}&page=0&size=24`,
        { auth: false },
      ),
    enabled: q.length > 0,
  });

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <h1 className="mb-2 text-2xl font-semibold tracking-tight">
        Resultados para "{q}"
      </h1>
      <p className="mb-6 text-sm text-muted-foreground">
        {data ? `${data.totalElements} produto(s)` : ""}
      </p>
      {isLoading && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Skeleton key={i} className="h-72 w-full rounded-xl" />
          ))}
        </div>
      )}
      {data && data.content.length === 0 && (
        <div className="rounded-lg border border-border bg-card p-10 text-center text-muted-foreground">
          Nenhum resultado encontrado.
        </div>
      )}
      {data && data.content.length > 0 && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
          {data.content.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </div>
  );
}