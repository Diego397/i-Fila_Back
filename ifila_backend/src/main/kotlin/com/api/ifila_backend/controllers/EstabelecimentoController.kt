package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.EstabelecimentoDTO
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.models.EstabelecimentoModel
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.EstabelecimentoService
import com.api.ifila_backend.services.UsuarioService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.BeanUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(
    "/estabelecimentos",
    produces = ["application/json"]
)
class EstabelecimentoController(val estabelecimentoService: EstabelecimentoService,
                                usuarioService: UsuarioService) : BaseController(usuarioService) {

    @PostMapping(consumes = ["application/json"])
    @ApiOperation(value = "Cadastra um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 201, message = "Estabelecimento cadastrado com sucesso", response = EstabelecimentoModel::class),
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun cadastrarUsuario(
        @ApiParam(name = "Estabelecimento", value = "Informações do estabelecimento")
        @RequestBody @Valid estabelecimentoDTO: EstabelecimentoDTO,
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        val usuarioModel: UsuarioModel = usuarioModelOptional.get()

        if (usuarioModel.estabelecimento != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(MensagemPadraoDTO("Usuário já possui estabelecimento!"))

        val estabelecimentoModel = EstabelecimentoModel()
        BeanUtils.copyProperties(estabelecimentoDTO, estabelecimentoModel)

        usuarioModel.estabelecimento = estabelecimentoModel

        return ResponseEntity.status(HttpStatus.CREATED).body(estabelecimentoService.save(estabelecimentoModel))
    }
}