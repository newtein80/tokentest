package com.example.tokentest.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import com.example.tokentest.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	@Value("${spring.jwt.secret}")
	private String jwtSecret;

	@Value("${spring.jwt.jwtExpirationMs}")
	private int jwtExpirationMs;

	public String generateJwtToken(Authentication authentication) {

		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		return Jwts.builder()
				.setSubject((userPrincipal.getUsername()))
				.setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				// .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .signWith(getSigningKey(jwtSecret), SignatureAlgorithm.HS256)
				.compact();
	}

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

	public String getUserNameFromJwtToken(String token) {
        Claims checkClaims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey(jwtSecret))
            .build()
            .parseClaimsJws(token)
            .getBody();

        // return checkClaims.get("sub", String.class);
		return Jwts.parserBuilder().setSigningKey(getSigningKey(jwtSecret)).build().parseClaimsJws(token).getBody().getSubject();
	}

    public Map<String, Object> getInfosFromJwtToken(String token) {
        Claims checkClaims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey(jwtSecret))
            .build()
            .parseClaimsJws(token)
            .getBody();

        return checkClaims;
		// return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			// Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            Jwts.parserBuilder()
            .setSigningKey(getSigningKey(jwtSecret))
            .build()
            .parseClaimsJws(authToken)
            ;
			return true;
		} catch (SignatureException e) {
			// logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			// logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			// logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			// logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			// logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
