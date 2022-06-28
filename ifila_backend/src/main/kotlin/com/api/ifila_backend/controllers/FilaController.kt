package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.dtos.UsuarioDTO
import com.api.ifila_backend.models.EstabelecimentoModel
import com.api.ifila_backend.models.UsuarioModel
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
import java.util.*
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(
    "/fila",
    produces = ["application/json"]
)
class FilaController (val filaService: FilaService,
                      usuarioService: UsuarioService) : BaseController(usuarioService) {

    @PostMapping("/{id}")
    @ApiOperation(value = "Entra na fila de um estabelecimento")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Informações de um Estabelecimento", response = EstabelecimentoModel::class),
//        ApiResponse(code = 404, message = "Estabelecimento não encontrado", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun entrarFila(
        @PathVariable (value = "id") filaId: UUID,
    ): ResponseEntity<Any> {

        when {
            filaService.existsByUUID(filaId) -> return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                MensagemPadraoDTO("Fila não existe")
            )
        }

        val filaModel = filaModel()
        BeanUtils.copyProperties(filaDTO, filaModel)

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(usuarioModel))
    }
}