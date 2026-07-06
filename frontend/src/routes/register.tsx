import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { useAuth } from "@/lib/auth-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { UserRole } from "@/lib/types";

export const Route = createFileRoute("/register")({
  component: RegisterPage,
});

function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phoneNumber: "",
    cpf: "",
    role: "ROLE_CLIENT" as UserRole,
  });
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await register(form);
      toast.success("Cadastro realizado!");
      navigate({ to: "/" });
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Falha no cadastro");
    } finally {
      setLoading(false);
    }
  }

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) =>
    setForm((f) => ({ ...f, [k]: v }));

  return (
    <div className="mx-auto grid min-h-[80vh] max-w-md place-items-center px-4 py-10">
      <div className="w-full rounded-2xl border border-border/60 bg-card p-8 shadow-2xl shadow-black/20">
        <h1 className="text-2xl font-semibold tracking-tight">Criar conta</h1>
        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="name">Nome completo</Label>
            <Input
              id="name"
              value={form.name}
              onChange={(e) => set("name", e.target.value)}
              minLength={2}
              maxLength={100}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              value={form.email}
              onChange={(e) => set("email", e.target.value.toLowerCase())}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="password">Senha (mínimo 6)</Label>
            <Input
              id="password"
              type="password"
              value={form.password}
              onChange={(e) => set("password", e.target.value)}
              minLength={6}
              maxLength={50}
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="phone">Telefone</Label>
              <Input
                id="phone"
                value={form.phoneNumber}
                onChange={(e) => set("phoneNumber", e.target.value)}
                placeholder="+5511999999999"
                pattern="\+?[0-9]{10,15}"
                required
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cpf">CPF (11 dígitos)</Label>
              <Input
                id="cpf"
                value={form.cpf}
                onChange={(e) => set("cpf", e.target.value.replace(/\D/g, ""))}
                maxLength={11}
                pattern="\d{11}"
                required
              />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label>Tipo de conta</Label>
            <Select
              value={form.role}
              onValueChange={(v) => set("role", v as UserRole)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ROLE_CLIENT">Cliente</SelectItem>
                <SelectItem value="ROLE_SELLER">Vendedor</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Criando..." : "Criar conta"}
          </Button>
        </form>
        <p className="mt-6 text-center text-sm text-muted-foreground">
          Já tem conta?{" "}
          <Link to="/login" className="text-primary hover:underline">
            Entrar
          </Link>
        </p>
      </div>
    </div>
  );
}