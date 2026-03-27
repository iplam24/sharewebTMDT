// src/main/java/com/shop/security/JwtService.java
package com.shop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // LƯU Ý: secret phải là BASE64, tối thiểu 32 bytes (256-bit) cho HS256
    @Value("${jwt.secret}")
    private String secretBase64;

    // thời gian sống token (ms)
    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    // lệch giờ cho phép khi parse (giây) – tuỳ chọn
    @Value("${jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    // ===== Public APIs =====
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(exp)
                // 0.12.x: có thể ghi rõ thuật toán hoặc để tự suy luận từ key
                .signWith(getSecretKey())                 // tự chọn HS256 từ key
                // .signWith(getSecretKey(), Jwts.SIG.HS256) // nếu muốn fix cứng
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // ===== Internals =====
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            // 0.12.x: verifyWith(...) + parseSignedClaims(...).getPayload()
            return Jwts.parser()
                    .clockSkewSeconds(clockSkewSeconds)
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            // token sai/hết hạn/giả mạo -> bắn ra cho layer trên xử lý
            throw ex;
        }
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
