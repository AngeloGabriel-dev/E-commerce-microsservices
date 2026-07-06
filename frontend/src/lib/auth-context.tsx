import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { apiFetch, decodeJwt, getToken, setToken } from "./api";
import type {
  JwtClaims,
  JwtToken,
  UserCreateDto,
  UserLoginDto,
  UserRole,
} from "./types";

interface AuthContextValue {
  token: string | null;
  claims: JwtClaims | null;
  isAuthenticated: boolean;
  roles: UserRole[];
  hasRole: (role: UserRole) => boolean;
  login: (dto: UserLoginDto) => Promise<void>;
  register: (dto: UserCreateDto) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function extractRoles(claims: JwtClaims | null): UserRole[] {
  if (!claims) return [];
  const bag: string[] = [];
  if (Array.isArray(claims.roles)) bag.push(...claims.roles);
  if (typeof claims.role === "string") bag.push(claims.role);
  if (Array.isArray(claims.role)) bag.push(...claims.role);
  if (Array.isArray(claims.authorities)) bag.push(...claims.authorities);
  const roles = bag
    .map((r) => (r.startsWith("ROLE_") ? r : `ROLE_${r}`))
    .filter((r): r is UserRole =>
      ["ROLE_ADMIN", "ROLE_SELLER", "ROLE_CLIENT"].includes(r),
    );
  return Array.from(new Set(roles));
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);

  useEffect(() => {
    setTokenState(getToken());
  }, []);

  const claims = useMemo(
    () => (token ? decodeJwt<JwtClaims>(token) : null),
    [token],
  );

  const applyToken = useCallback((t: string | null) => {
    setToken(t);
    setTokenState(t);
  }, []);

  const login = useCallback(
    async (dto: UserLoginDto) => {
      const res = await apiFetch<JwtToken>("/api/v1/auth/login", {
        method: "POST",
        auth: false,
        body: JSON.stringify(dto),
      });
      applyToken(res.token);
    },
    [applyToken],
  );

  const register = useCallback(
    async (dto: UserCreateDto) => {
      await apiFetch("/api/v1/auth/register", {
        method: "POST",
        auth: false,
        body: JSON.stringify(dto),
      });
      await login({ email: dto.email, password: dto.password });
    },
    [login],
  );

  const logout = useCallback(() => applyToken(null), [applyToken]);

  const roles = useMemo(() => extractRoles(claims), [claims]);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      claims,
      isAuthenticated: Boolean(token),
      roles,
      hasRole: (r) => roles.includes(r),
      login,
      register,
      logout,
    }),
    [token, claims, roles, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}