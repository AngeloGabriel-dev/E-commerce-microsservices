import { Link, useNavigate } from "@tanstack/react-router";
import { ShoppingBag, Search, LogOut, Store, Package, User } from "lucide-react";
import { useState, type FormEvent } from "react";
import { useAuth } from "@/lib/auth-context";
import { useCart } from "@/lib/cart-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function SiteHeader() {
  const { isAuthenticated, hasRole, logout, claims } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();
  const [q, setQ] = useState("");

  function onSearch(e: FormEvent) {
    e.preventDefault();
    const query = q.trim();
    if (!query) return;
    navigate({ to: "/search", search: { q: query } });
  }

  return (
    <header className="sticky top-0 z-40 border-b border-border/60 bg-background/70 backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-4 px-4">
        <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight">
          <div className="grid h-8 w-8 place-items-center rounded-md bg-primary text-primary-foreground">
            <ShoppingBag className="h-4 w-4" />
          </div>
          <span className="text-gradient-gold text-lg">NEXA</span>
        </Link>

        <form onSubmit={onSearch} className="relative ml-4 hidden flex-1 md:block">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Buscar produtos..."
            className="pl-9"
          />
        </form>

        <nav className="ml-auto flex items-center gap-1">
          {hasRole("ROLE_SELLER") && (
            <Button asChild variant="ghost" size="sm">
              <Link to="/seller">
                <Store className="mr-1.5 h-4 w-4" />
                Vendedor
              </Link>
            </Button>
          )}
          {isAuthenticated && (
            <Button asChild variant="ghost" size="sm">
              <Link to="/orders">
                <Package className="mr-1.5 h-4 w-4" />
                Pedidos
              </Link>
            </Button>
          )}
          <Button asChild variant="ghost" size="sm" className="relative">
            <Link to="/cart">
              <ShoppingBag className="h-4 w-4" />
              {count > 0 && (
                <span className="absolute -right-1 -top-1 grid h-5 min-w-5 place-items-center rounded-full bg-primary px-1 text-[11px] font-bold text-primary-foreground">
                  {count}
                </span>
              )}
            </Link>
          </Button>
          {isAuthenticated ? (
            <>
              <span className="ml-2 hidden text-sm text-muted-foreground sm:inline">
                <User className="mr-1 inline h-3.5 w-3.5" />
                {claims?.email ?? claims?.sub ?? "conta"}
              </span>
              <Button variant="ghost" size="sm" onClick={logout}>
                <LogOut className="h-4 w-4" />
              </Button>
            </>
          ) : (
            <>
              <Button asChild variant="ghost" size="sm">
                <Link to="/login">Entrar</Link>
              </Button>
              <Button asChild size="sm">
                <Link to="/register">Cadastrar</Link>
              </Button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}