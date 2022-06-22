package com.api.ifila_backend.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component

@Component
class JWTUtils {

    private val chaveSeguranca = "verysafekey"

    fun gerarToken(idUsuario: String, roleUsuario: String) : String {
        return Jwts.builder()
            .setSubject(idUsuario)
            .signWith(SignatureAlgorithm.HS512, chaveSeguranca.toByteArray())
            .compact()
    }

    fun verificarTokenValido(token: String) : Boolean {
        val claims = getClaimsToken(token)

        if (claims != null) {
            val idUsuario = claims.subject
            if (!idUsuario.isNullOrEmpty() && idUsuario.isNotBlank()) {
                return true
            }
        }

        return false
    }

    private fun getClaimsToken(token: String): Claims? {
        return try {
            Jwts.parser().setSigningKey(chaveSeguranca.toByteArray()).parseClaimsJws(token).body
        } catch (exception: Exception) {
            null
        }
    }

    fun getUsuarioId(token: String) : String? {
        val claims = getClaimsToken(token)
        return claims?.subject
    }
}