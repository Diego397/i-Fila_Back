package com.api.ifila_backend.filters

import com.api.ifila_backend.impl.UsuarioDetalheImpl
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.JWTUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAutorizadorFilter(authenticationManager: AuthenticationManager, val jwtUtils: JWTUtils, val usuarioService: UsuarioService)
    : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorization = request.getHeader("Authorization")

        if (authorization != null && authorization.startsWith("Bearer")) {
            val autorizado = getAuthentication(authorization)
            SecurityContextHolder.getContext().authentication = autorizado
        }

        chain.doFilter(request, response)
    }

    private fun getAuthentication(authorization: String): UsernamePasswordAuthenticationToken {
        // Retirar "Bearer " do header
        val token = authorization.substring(7)

        if (jwtUtils.verificarTokenValido(token)) {
            val idString = jwtUtils.getUsuarioId(token)

            val usuario = usuarioService.findByIdOrNull(UUID.fromString(idString))
                ?: throw UsernameNotFoundException("Usuário não encontrado")

            val usuarioImpl = UsuarioDetalheImpl(usuario)

            return UsernamePasswordAuthenticationToken(usuarioImpl, null, usuarioImpl.authorities)
        }

        throw UsernameNotFoundException("Token inválido")
    }
}