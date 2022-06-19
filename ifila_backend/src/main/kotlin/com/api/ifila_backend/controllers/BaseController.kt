package com.api.ifila_backend.controllers

import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.JWTUtils
import java.util.*

open class BaseController (open val usuarioService: UsuarioService) {

    fun lerToken(authorization: String): Optional<UsuarioModel> {
        val token = authorization.substring(7)
        val usuarioIdString = JWTUtils().getUsuarioId(token)

        val usuarioModelOptional = usuarioService.findById(UUID.fromString(usuarioIdString))

        if(!usuarioModelOptional.isPresent)
            throw java.lang.IllegalArgumentException("Usuário não encontrado")

        return usuarioModelOptional
    }
}