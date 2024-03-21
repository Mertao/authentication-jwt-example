package com.mertao.authenticationjwtexample.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mertao.authenticationjwtexample.config.Role;
import com.mertao.authenticationjwtexample.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtService {
	@Value("${token.signin.key}")
	private String jwtSigningKey;
	
	/**
     * Извлечение имени пользователя из токена
     *
     * @param token токен
     * @return имя пользователя
     */
	public String extractUserName(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	/**
     * Генерация токена
     *
     * @param userDetails данные пользователя
     * @return токен
     */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		if (userDetails instanceof User customUserDetails) {
			claims.put("id", customUserDetails.getId());
			claims.put("email", customUserDetails.getId());
			Set<String> roles = customUserDetails.getRoles().stream().map(Role::name).collect(Collectors.toSet());
			claims.put("roles", roles);
		}
		return generateToken(claims, userDetails);
	}
	
	/**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @param userDetails данные пользователя
     * @return true, если токен валиден
     */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUserName(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	/**
     * Извлечение данных из токена
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
		final Claims claims = extractAllClaims(token);
		return claimsResolvers.apply(claims);
	}
	
	/**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
	private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return Jwts.builder()
                .claims(extraClaims)
				.subject(userDetails.getUsername())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 100000 * 60 * 24))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}
	
	/**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	/**
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	/**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}
	
	/**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
