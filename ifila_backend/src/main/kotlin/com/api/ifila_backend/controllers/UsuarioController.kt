package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.UsuarioDTO
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.UsuarioService
import org.springframework.beans.BeanUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/usuarios")
class UsuarioController (val usuarioService: UsuarioService){

    @PostMapping
    fun cadastrarUsuario(@RequestBody @Valid usuarioDTO: UsuarioDTO):Any{

        when{
            usuarioService.existsByEmail(usuarioDTO.email) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "email já cadastrado."))
            usuarioService.existsByCpf(usuarioDTO.cpf) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "cpf já cadastrado"))
        }

        val usuarioModel = UsuarioModel()
        BeanUtils.copyProperties(usuarioDTO, usuarioModel)

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(usuarioModel))
    }
}