import { Link } from "@tanstack/react-router";
import type { Product } from "@/lib/types";
import { formatBRL } from "@/lib/cart-context";

export function ProductCard({ product }: { product: Product }) {
  const image = product.imageUrls?.[0];
  return (
    <Link
      to="/products/$id"
      params={{ id: product.id }}
      className="group flex flex-col overflow-hidden rounded-xl border border-border/60 bg-card transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-lg hover:shadow-primary/5"
    >
      <div className="relative aspect-square overflow-hidden bg-muted">
        {image ? (
          <img
            src={image}
            alt={product.name}
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="grid h-full w-full place-items-center bg-gradient-to-br from-muted to-secondary text-4xl font-black text-muted-foreground/40">
            {product.name.charAt(0).toUpperCase()}
          </div>
        )}
        {product.stock === 0 && (
          <div className="absolute inset-0 grid place-items-center bg-background/70 text-sm font-medium">
            Esgotado
          </div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-1 p-4">
        <span className="text-xs uppercase tracking-wider text-muted-foreground">
          {product.category}
        </span>
        <h3 className="line-clamp-2 font-medium leading-tight">{product.name}</h3>
        <div className="mt-auto pt-2 text-lg font-semibold text-gradient-gold">
          {formatBRL(product.price)}
        </div>
      </div>
    </Link>
  );
}