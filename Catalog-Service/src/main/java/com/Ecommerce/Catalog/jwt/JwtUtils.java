package com.Ecommerce.Catalog.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
public class JwtUtils {
    public static final String JWT_BEARER = "Bearer ";
    public static final String JWT_AUTHORIZATION = "Authorization";
    public static final String SECRET_KEY = "0123456789-0123456789-0123456789";

    private JwtUtils(){}

    private static Key generateKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private static Claims getClaimsFromToken(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(generateKey()).build()
                    .parseClaimsJws(refactorToken(token)).getBody();
        }catch (JwtException ex){
            log.error(String.format("Token invalido: %s", ex.getMessage()));
        }
        return null;
    }

    public static String getUsernameFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    public static String getRoleFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    public static String getUserIdFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("userId", String.class);
        }
        return null;
    }

    public static boolean isTokenValid(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(generateKey()).build()
                    .parseClaimsJws(refactorToken(token));
            return true;
        }catch (JwtException ex){
            log.error(String.format("Token invalido: %s", ex.getMessage()));
        }
        return false;
    }

    private static String refactorToken(String token){
        if(token.contains(JWT_BEARER)){
            return token.substring(JWT_BEARER.length());
        }
        return token;
    }
}
