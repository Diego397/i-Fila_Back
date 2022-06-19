package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.LoginDTO
import com.api.ifila_backend.dtos.LoginRespostaDTO
import com.api.ifila_backend.dtos.UsuarioDTO
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.utils.JWTUtils
import io.swagger.annotations.*
import org.springframework.beans.BeanUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.Optional
import java.util.UUID
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(
    "/usuarios",
    produces = ["application/json"]
)
class UsuarioController (usuarioService: UsuarioService) : BaseController(usuarioService) {

    @PostMapping(consumes = ["application/json"])
    @ApiOperation(value = "Cadastra um usuário")
    @ApiResponses(
        ApiResponse(code = 201, message = "Usuário cadastrado com sucesso", response = UsuarioModel::class),
        ApiResponse(code = 409, message = "Conflito com dados salvos", response = MensagemPadraoDTO::class)
    )
    fun cadastrarUsuario(
        @ApiParam(name = "User", value = "Informações do usuário")
        @RequestBody @Valid usuarioDTO: UsuarioDTO
    ): ResponseEntity<Any> {

        when {
            usuarioService.existsByEmail(usuarioDTO.email) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(MensagemPadraoDTO("Email já cadastrado."))
            usuarioService.existsByCpf(usuarioDTO.cpf) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(MensagemPadraoDTO("Cpf já cadastrado"))
        }

        val usuarioModel = UsuarioModel()
        BeanUtils.copyProperties(usuarioDTO, usuarioModel)

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(usuarioModel))
    }

    @GetMapping
    @ApiOperation(value = "Retorna uma lista de usuários")
    @ApiResponses(
        ApiResponse(code = 200, message = "Lista de usuários", response = UsuarioModel::class, responseContainer = "List"),
    )
    fun getUsuarios(): ResponseEntity<List<UsuarioModel>> {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.findAll())
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Retorna um usuário")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações de um Usuário", response = UsuarioModel::class),
        ApiResponse(code = 404, message = "Usuário não encontrado", response = MensagemPadraoDTO::class)
    )
    fun getUsuario(@PathVariable (value = "id") id:UUID): ResponseEntity<Any> {

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)
        if(!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        return ResponseEntity.status(HttpStatus.OK).body(usuarioModelOptional)
    }

    @PutMapping("/{id}", consumes = ["application/json"])
    @ApiOperation(value = "Atualiza as informações de um usuário")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações atualizadas do Usuário", response = UsuarioModel::class),
        ApiResponse(code = 404, message = "Usuário não encontrado", response = MensagemPadraoDTO::class),
        ApiResponse(code = 409, message = "Conflito com dados salvos", response = MensagemPadraoDTO::class)
    )
    fun putUsuario(@PathVariable (value = "id") id:UUID,
                   @ApiParam(name = "User", value = "Informações do usuário")
                   @RequestBody @Valid usuarioDTO: UsuarioDTO): ResponseEntity<Any>{

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)

        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        if(usuarioDTO.email != usuarioModelOptional.get().email && usuarioService.existsByEmail(usuarioDTO.email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(MensagemPadraoDTO("Email já cadastrado."))

        if (usuarioDTO.cpf != usuarioModelOptional.get().cpf && usuarioService.existsByCpf(usuarioDTO.cpf))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(MensagemPadraoDTO("Cpf já cadastrado"))

        val usuarioModel = UsuarioModel()
        BeanUtils.copyProperties(usuarioDTO, usuarioModel)
        usuarioModel.id = usuarioModelOptional.get().id
        usuarioModel.dataDeCriacao = usuarioModelOptional.get().dataDeCriacao

        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.save(usuarioModel))
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Deleta um usuário")
    @ApiResponses(
        ApiResponse(code = 200, message = "Usuário removido com sucesso", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Usuário não encontrado", response = MensagemPadraoDTO::class),
    )
    fun deleteUsuario(@PathVariable(value = "id") id: UUID): ResponseEntity<Any> {

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)

        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        usuarioService.delete(usuarioModelOptional.get())

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Usuário removido com sucesso"))
    }

    @GetMapping("/me")
    @ApiOperation(value = "Retorna informações do usuário logado")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações do usuário logado", response = UsuarioModel::class)
    )
    fun getUsuarioLogado(@ApiIgnore @RequestHeader("Authorization") authorization: String): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)

        return ResponseEntity.status(HttpStatus.OK).body(usuarioModelOptional)
    }
}