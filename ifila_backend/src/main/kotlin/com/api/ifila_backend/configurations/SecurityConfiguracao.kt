package com.api.ifila_backend.configurations

import com.api.ifila_backend.filters.JWTAutorizadorFilter
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.JWTUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguracao : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var jwtUtils: JWTUtils

    @Autowired
    private lateinit var usuarioService: UsuarioService

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

        http.addFilter(JWTAutorizadorFilter(authenticationManager(), jwtUtils, usuarioService))
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Bean
    fun configuracaoCors() : CorsConfigurationSource? {
        val configuracao = CorsConfiguration()

        configuracao.allowedOrigins = mutableListOf("*")
        configuracao.allowedMethods = mutableListOf("*")
        configuracao.allowedHeaders = mutableListOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuracao)
        return source
    }
}