package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.LoginDTO
import com.api.ifila_backend.dtos.LoginRespostaDTO
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.JWTUtils
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(
    "/auth",
    produces = ["application/json"]
)
class AuthController (val usuarioService: UsuarioService) {

    @PostMapping("/login", consumes = ["application/json"])
    @ApiOperation(value = "Login de usuário")
    fun loginUsuario(
        @ApiParam(name = "Login", value = "Informações de login")
        @RequestBody @Valid body: LoginDTO
    ): ResponseEntity<Any> {
        val usuario = usuarioService.findByEmail(body.email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensagem" to "Email não cadastrado!"))

        if (!usuario.checarSenha(body.senha)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("mensagem" to "Senha incorreta!"))
        }

        val token = JWTUtils().gerarToken(idUsuario = usuario.id.toString())

        return ResponseEntity.status(HttpStatus.OK).body(LoginRespostaDTO(token))
    }
}