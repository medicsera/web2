package com.example.spring_jpa.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(email: String, role: String): String =
        Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    fun extractEmail(token: String): String =
        extractAllClaims(token).subject

    fun extractRole(token: String): String =
        extractAllClaims(token)["role"] as String

    fun isTokenValid(token: String): Boolean =
        try {
            !extractAllClaims(token).expiration.before(Date())
        } catch (e: JwtException) {
            false
        }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
