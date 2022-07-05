package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.LoginDTO
import com.api.ifila_backend.dtos.LoginRespostaDTO
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.JWTUtils
import io.swagger.annotations.*
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
    @ApiResponses(
        ApiResponse(code = 200, message = "Token de acesso", response = LoginRespostaDTO::class),
        ApiResponse(code = 404, message = "Email não cadastrado", response = MensagemPadraoDTO::class),
        ApiResponse(code = 400, message = "Senha incorreta", response = MensagemPadraoDTO::class)
    )
    fun loginUsuario(
        @ApiParam(name = "Login", value = "Informações de login")
        @RequestBody @Valid body: LoginDTO
    ): ResponseEntity<Any> {
        val usuario = usuarioService.findByEmail(body.email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Email não cadastrado!"))

        if (!usuario.checarSenha(body.senha)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Senha incorreta!"))
        }

        val token = JWTUtils().gerarToken(idUsuario = usuario.id.toString())

        return ResponseEntity.status(HttpStatus.OK).body(LoginRespostaDTO(token, usuario.role))
    }
}