package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.AbrirFilaDTO
import com.api.ifila_backend.dtos.EntrarFilaDTO
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.dtos.UsuarioDTO
import com.api.ifila_backend.models.EstabelecimentoModel
import com.api.ifila_backend.models.FilaModel
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.EstabelecimentoService
import com.api.ifila_backend.services.FilaService
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
    "/fila",
    produces = ["application/json"]
)
class FilaController (val filaService: FilaService,
                      val estabelecimentoService: EstabelecimentoService,
                      usuarioService: UsuarioService) : BaseController(usuarioService) {

    @PostMapping("/entrarfila")
    @ApiOperation(value = "Entra na fila de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun entrarFila(
        @RequestBody @Valid entrarFilaDTO: EntrarFilaDTO,
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(entrarFilaDTO.codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido"))

        val filaModel = filaModelOptional.get()

        if (filaModel.estabelecimento.statusFila == false)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Estabelecimento Fechado!"))

        if (entrarFilaDTO.tipoFila == "prioridade") {
            filaModel.filaPrioridade.add(usuarioModel.id!!)
        } else {
            filaModel.filaPrincipal.add(usuarioModel.id!!)
        }

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Cliente entrou na fila!"))
    }

    @PostMapping("/abrirfila")
    @ApiOperation(value = "Abrir fila do estabelecimento")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Fila Aberta", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Usuario não encontrado", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun abrirFila(
        @RequestBody @Valid abrirFilaDTO: AbrirFilaDTO,
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val estabelecimentoModel = usuarioModel.estabelecimento
        if (estabelecimentoModel == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não encontrado!"))

        estabelecimentoModel.statusFila = true
        estabelecimentoModel.fila!!.capacidadeMaxima = abrirFilaDTO.capacidadeMaxima
        estabelecimentoModel.fila!!.horarioMaximo = abrirFilaDTO.horarioMaximoEntrada

        estabelecimentoService.save(estabelecimentoModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Estabelecimento aberto!"))
    }

//    @GetMapping
//    @ApiOperation(value = "Retorna a lista de id de uma fila")
//    @ApiResponses(
//        ApiResponse(code = 200, message = "Lista de usuários na fila", response = UsuarioModel::class, responseContainer = "List"),
//    )
//    fun getFila(
//        @RequestBody @Valid entrarFilaDTO: EntrarFilaDTO,
//    ): ResponseEntity<List<UUID>> {
//        return ResponseEntity.status(HttpStatus.OK).body(filaService.findByFilaPrincipal())
//    }
}