package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.EstabelecimentoDTO
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.dtos.UsuarioDTO
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
import java.util.*
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
        ApiResponse(code = 400, message = "Usuário não encontrado", response = MensagemPadraoDTO::class),
        ApiResponse(code = 409, message = "Conflito com dados salvos", response = MensagemPadraoDTO::class)
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

    @GetMapping
    @ApiOperation(value = "Retorna uma lista de estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Lista de estabelecimentos", response = EstabelecimentoModel::class, responseContainer = "List"),
    )
    fun getEstabelecimentos(): ResponseEntity<List<EstabelecimentoModel>> {
        return ResponseEntity.status(HttpStatus.OK).body(estabelecimentoService.findAll())
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Retorna um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações de um Estabelecimento", response = EstabelecimentoModel::class),
        ApiResponse(code = 404, message = "Estabelecimento não encontrado", response = MensagemPadraoDTO::class)
    )
    fun getEstabelecimento(@PathVariable (value = "id") id: UUID): ResponseEntity<Any> {

        val estabelecimentoModelOptional: Optional<EstabelecimentoModel> = estabelecimentoService.findById(id)
        if(!estabelecimentoModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não encontrado!"))

        return ResponseEntity.status(HttpStatus.OK).body(estabelecimentoModelOptional)
    }

    @PutMapping("/{id}", consumes = ["application/json"])
    @ApiOperation(value = "Atualiza as informações de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações atualizadas do Estabelecimento", response = EstabelecimentoModel::class),
        ApiResponse(code = 403, message = "Você não tem autorização para modificar os dados desse estabelecimento", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class),
        ApiResponse(code = 409, message = "Conflito com dados salvos", response = MensagemPadraoDTO::class)
    )
    fun putEstabelecimento(@PathVariable (value = "id") id:UUID,
                   @ApiParam(name = "Estabelecimento", value = "Informações do estabelecimento")
                   @RequestBody @Valid estabelecimentoDTO: EstabelecimentoDTO,
                   @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any>{

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        val estabelecimentoModelOptional: Optional<EstabelecimentoModel> = estabelecimentoService.findById(id)

        if (!estabelecimentoModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não encontrado"))

        if (usuarioModelOptional.get().estabelecimento != estabelecimentoModelOptional.get())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MensagemPadraoDTO("Você não tem autorização para modificar as informações desse estabelecimento"))

        var estabelecimentoModel = EstabelecimentoModel()

        BeanUtils.copyProperties(estabelecimentoDTO, estabelecimentoModel)
        estabelecimentoModel.id = estabelecimentoModelOptional.get().id
        estabelecimentoModel.dataDeCriacao = estabelecimentoModelOptional.get().dataDeCriacao

        return ResponseEntity.status(HttpStatus.OK).body(estabelecimentoService.save(estabelecimentoModel))
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Deleta um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Estabelecimento removido com sucesso", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Estabelecimento não encontrado", response = MensagemPadraoDTO::class),
    )
    fun deleteEstabelecimento(@PathVariable(value = "id") id: UUID,
                      @ApiIgnore @RequestHeader("Authorization") authorization: String): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        val estabelecimentoModelOptional: Optional<EstabelecimentoModel> = estabelecimentoService.findById(id)

        if (!estabelecimentoModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não encontrado"))

        if (usuarioModelOptional.get().estabelecimento != estabelecimentoModelOptional.get())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MensagemPadraoDTO("Você não tem autorização para deletar esse estabelecimento"))

        estabelecimentoService.delete(estabelecimentoModelOptional.get())

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Estabelecimento removido com sucesso"))
    }

    @GetMapping("/me")
    @ApiOperation(value = "Retorna informações do estabelecimento do usuário logado")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações do Estabelecimento", response = EstabelecimentoModel::class),
        ApiResponse(code = 404, message = "Usuário não encontrado", response = MensagemPadraoDTO::class)
    )
    fun getEstabelecimentoLogado(@ApiIgnore @RequestHeader("Authorization") authorization: String): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))

        return ResponseEntity.status(HttpStatus.OK).body(usuarioModelOptional.get().estabelecimento)
    }
}