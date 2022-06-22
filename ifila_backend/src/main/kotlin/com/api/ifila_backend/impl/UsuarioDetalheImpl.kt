package com.api.ifila_backend.impl

import com.api.ifila_backend.models.UsuarioModel
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UsuarioDetalheImpl(private val usuario: UsuarioModel) : UserDetails {
    override fun getAuthorities() = mutableListOf<GrantedAuthority>(SimpleGrantedAuthority(usuario.role))

    override fun getPassword() = usuario.senha

    override fun getUsername() = usuario.email

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}