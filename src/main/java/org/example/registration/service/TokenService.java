package org.example.registration.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class TokenService {

    public String generateToken(String name) {

        Map<String,Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(name)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public void setTokenToSession(String jwtToken , HttpSession session){
        session.setAttribute("jwtToken", jwtToken); //save our token in session
        log.info("jwt token is added to session : {} " , jwtToken);
    }

    private Key getSignKey() {
        byte[] bytes = Decoders.BASE64.decode("Y2Y5S45ffDAwqwzU31Rni5iJY81Czd01e3jrz+Zkyhvo9qpMJMYiPLDpCdHzXFco");
        return Keys.hmacShaKeyFor(bytes);
    }

    public boolean validateToken(String token , UserDetails userDetails){
        String name = extractUserName(token);
        return name.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUserName(String token){
        return extractClaim(token , Claims::getSubject);
    }

    private boolean isTokenExpired(String token){
        return extractExparationDate(token).before(new Date());
    }

    private Date extractExparationDate(String token){
        return extractClaim(token , Claims::getExpiration);
    }

    public<T>  T  extractClaim(String token, Function<Claims , T > function) {

        Claims claims = getAllClaims(token);
        return function.apply(claims);

    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
