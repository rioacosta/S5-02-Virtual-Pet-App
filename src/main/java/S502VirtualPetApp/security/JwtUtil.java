package S502VirtualPetApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:mySuperSecretKey}")
    private String secretKey;

    @Value("${jwt.expiration:86400}")
    private int jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        claims.put("roles", roles);  // 🌈 Añade el claim correcto

        return createToken(claims, userDetails.getUsername());
    }



    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            logger.info("JWT token validation - waiting: {}, extracted: {}", userDetails.getUsername(), username);
            logger.info("¿Token expirado?: {}", isTokenExpired(token));

            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
