package com.api.ifila_backend.configurations

import com.api.ifila_backend.filters.JWTAutorizadorFilter
import com.api.ifila_backend.repositories.UsuarioRepository
import com.api.ifila_backend.utils.JWTUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfiguracao : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var jwtUtils: JWTUtils

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    override fun configure(http: HttpSecurity) {
        http.csrf().disable().authorizeRequests()
            .antMatchers(HttpMethod.POST, "/usuarios", "/auth/login").permitAll()
            .antMatchers(HttpMethod.GET,"/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/swagger-ui/",
                "/webjars/**").permitAll()
            .anyRequest().authenticated()

        http.addFilter(JWTAutorizadorFilter(authenticationManager(), jwtUtils, usuarioRepository))
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}