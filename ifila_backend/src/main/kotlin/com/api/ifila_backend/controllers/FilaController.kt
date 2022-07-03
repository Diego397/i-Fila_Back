package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.AbrirFilaDTO
import com.api.ifila_backend.dtos.InfoFilaEstabelecimentoDTO
import com.api.ifila_backend.dtos.InfoPosicaoFilaDTO
import com.api.ifila_backend.dtos.MensagemPadraoDTO
import com.api.ifila_backend.models.FilaModel
import com.api.ifila_backend.models.UsuarioInfoFilaModel
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.EstabelecimentoService
import com.api.ifila_backend.services.FilaService
import com.api.ifila_backend.services.UsuarioInfoFilaService
import com.api.ifila_backend.services.UsuarioService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
                      val usuarioInfoFilaService: UsuarioInfoFilaService,
                      usuarioService: UsuarioService) : BaseController(usuarioService) {

    // Rotas que podem ser acessadas pelos estabelecimentos

    @PostMapping("/abrir")
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
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não cadastrado!"))

        if (estabelecimentoModel.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila já aberta"))
        val filaModel = estabelecimentoModel.fila!!

        estabelecimentoModel.statusFila = true
        filaModel.capacidadeMaxima = abrirFilaDTO.capacidadeMaxima
        filaModel.horarioMaximo = abrirFilaDTO.horarioMaximoEntrada
        filaModel.clienteConfirmouPresenca = false
        filaModel.chamarCliente = false

        estabelecimentoService.save(estabelecimentoModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Fila aberta!"))
    }

    @PutMapping("/fechar")
    @ApiOperation(value = "Fecha fila de um estabelecimento")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun fecharFila(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val estabelecimentoModel = usuarioModel.estabelecimento
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não cadastrado!"))

        if (!estabelecimentoModel.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila não está aberta!"))

        val filaModel = estabelecimentoModel.fila!!

        estabelecimentoModel.statusFila = false

        filaModel.capacidadeMaxima = null
        filaModel.horarioMaximo = null
        filaModel.filaPrioridade.forEach {
            removerUsuarioFila(it)
        }
        filaModel.filaPrincipal.forEach {
            removerUsuarioFila(it)
        }

        filaModel.filaPrioridade.clear()
        filaModel.filaPrincipal.clear()

        estabelecimentoService.save(estabelecimentoModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Fila fechada!"))
    }

    @GetMapping("/info")
    @ApiOperation(value = "Retorna as informações da fila atual do estabelecimento")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Lista de usuários na fila", response = UsuarioModel::class, responseContainer = "List"),
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun getInfoFila(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val estabelecimentoModel = usuarioModel.estabelecimento
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não cadastrado!"))

        if (!estabelecimentoModel.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila não está aberta!"))

        val filaModel = estabelecimentoModel.fila!!

        val tamanhoFilaPrincipal = filaModel.filaPrincipal.size
        val tamanhoFilaPrioridade = filaModel.filaPrioridade.size

        return ResponseEntity.status(HttpStatus.OK).body(InfoFilaEstabelecimentoDTO(
            tamanhoFilaPrincipal = tamanhoFilaPrincipal,
            tamanhoFilaPrioridade = tamanhoFilaPrioridade,
            maximoPessoasFila = filaModel.capacidadeMaxima!!,
            tempoMedio = LocalTime.parse("00:05"),
            horarioMaximoEntrada = filaModel.horarioMaximo!!,
            chamarCliente = filaModel.chamarCliente,
            clienteConfirmouPresenca = filaModel.clienteConfirmouPresenca
        ))
    }

    @PutMapping("/chamarcliente")
    @ApiOperation(value = "Solicita presença do próximo cliente")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun chamarCliente(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val estabelecimentoModel = usuarioModel.estabelecimento
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não cadastrado!"))

        if (!estabelecimentoModel.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila não está aberta!"))

        val filaModel = estabelecimentoModel.fila!!

        if (filaModel.chamarCliente)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Estabelecimento já solicitou a presença do cliente!"))

        filaModel.chamarCliente = true
        filaService.save(filaModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Presença do cliente solicitada!"))
    }

    @PutMapping("/atendercliente")
    @ApiOperation(value = "Solicita confirmação do próximo cliente de uma fila")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun atenderCliente(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        val estabelecimentoModel = usuarioModel.estabelecimento
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Estabelecimento não cadastrado!"))

        if (!estabelecimentoModel.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila não está aberta!"))

        val filaModel = estabelecimentoModel.fila!!

        if (!filaModel.chamarCliente)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Estabelecimento não solicitou a presença do cliente!"))

        if (!filaModel.clienteConfirmouPresenca)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Cliente não confirmou a presença!"))

        // A FAZER!

        // Se tiver gnt na prioridade, atender usuário da prioridade
        // Se não, atender da fila principal
        // Remover primeiro usuário da fila
        // Utilizar função private removerUsuarioFila pra remover informações da fila no usuário

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Cliente não confirmou a presença!"))
    }


    // Rotas que podem ser acessadas pelos usuários

    @PutMapping("/entrar/{codigoFila}")
    @ApiOperation(value = "Entra na fila de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun entrarFila(
        @PathVariable (value = "codigoFila") codigoFila: String,
        @RequestParam(name="Tipo de Fila", defaultValue = "principal", required = false) tipoFila: String,
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {

        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        if (usuarioModel.emFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Usuário já está em uma fila!"))

        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido!"))
        val filaModel = filaModelOptional.get()

        if (!filaModel.estabelecimento.statusFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila do estabelecimento fechada!"))

        val quantidadePessoasFila: Int = filaModel.filaPrincipal.size + filaModel.filaPrioridade.size
        if (quantidadePessoasFila > filaModel.capacidadeMaxima!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Fila com capacidade máxima atingida!"))

        // Checar se ultrapassou horário máximo
        val formatoHorario = DateTimeFormatter.ofPattern("HH:mm")
        val horarioAtual = LocalTime.now(ZoneId.of("America/Fortaleza")).format(formatoHorario)

        val horasAtual = horarioAtual.substringBefore(":").toInt()
        val horasMaximo = filaModel.horarioMaximo.toString().substringBefore(":").toInt()
        val minutosAtual = horarioAtual.substringAfter(":").toInt()
        val minutosMaximo = filaModel.horarioMaximo.toString().substringAfter(":").take(2).toInt()

        if (horasAtual > horasMaximo || (horasAtual == horasMaximo && minutosAtual > minutosMaximo))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Horário máximo da fila ultrapassado!"))

        val posicaoCliente: Int
        if (tipoFila == "prioridade") {
            filaModel.filaPrioridade.add(usuarioModel.id!!)
            posicaoCliente = filaModel.filaPrioridade.size
        } else {
            filaModel.filaPrincipal.add(usuarioModel.id!!)
            posicaoCliente = filaModel.filaPrincipal.size + filaModel.filaPrioridade.size
        }

        filaService.save(filaModel)

        usuarioModel.emFila = true
        usuarioModel.infoFila = UsuarioInfoFilaModel(
            tipoFila = tipoFila, codigoFila = codigoFila
        )

        usuarioService.save(usuarioModel)

        return ResponseEntity.status(HttpStatus.OK).body(InfoPosicaoFilaDTO(posicao = posicaoCliente, deveConfirmarPresenca = false, tempoMedio = LocalTime.parse("00:05")))
    }

    @GetMapping("/infoposicao")
    @ApiOperation(value = "Retorna as informações da fila atual do usuário")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Lista de usuários na fila", response = UsuarioModel::class, responseContainer = "List"),
    )
    @PreAuthorize("hasRole('usuario')")
    fun getInfoPosicaoAtual(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        if (!usuarioModel.emFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Usuário não está em fila!"))

        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(usuarioModel.infoFila!!.codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido!"))
        val filaModel = filaModelOptional.get()

        val posicaoCliente: Int

        if (usuarioModel.infoFila!!.tipoFila == "prioridade")
            posicaoCliente = filaModel.filaPrioridade.indexOf(usuarioModel.id) + 1
        else
            posicaoCliente = filaModel.filaPrincipal.indexOf(usuarioModel.id) + 1 + filaModel.filaPrioridade.size

        var deveConfirmarPresenca: Boolean = false
        if (posicaoCliente == 1 && filaModel.chamarCliente && !filaModel.clienteConfirmouPresenca)
            deveConfirmarPresenca = true

        return ResponseEntity.status(HttpStatus.OK).body(InfoPosicaoFilaDTO(posicao = posicaoCliente, deveConfirmarPresenca = deveConfirmarPresenca, tempoMedio = LocalTime.parse("00:05")))
    }

    @PutMapping("/sair")
    @ApiOperation(value = "Sair da fila de um estabelecimento")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun sairFila(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        if (!usuarioModel.emFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Usuário não está em fila!"))

        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(usuarioModel.infoFila!!.codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido!"))
        val filaModel = filaModelOptional.get()

        if (usuarioModel.infoFila!!.tipoFila == "prioridade")
            filaModel.filaPrioridade.remove(usuarioModel.id)
        else
            filaModel.filaPrincipal.remove(usuarioModel.id)

        removerUsuarioFila(usuarioModel.id!!)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Usuário removido da fila!"))
    }

    @PutMapping("/confirmarpresenca")
    @ApiOperation(value = "Confirmar presença na fila")
    @ApiResponses(
//        ApiResponse(code = 200, message = "Cliente entrou na fila", response = MensagemPadraoDTO::class),
//        ApiResponse(code = 404, message = "Código da fila inválido", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun confirmarPresencaFila(
        @ApiIgnore @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Any> {
        val usuarioModelOptional = lerToken(authorization)
        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Usuário não encontrado!"))
        val usuarioModel = usuarioModelOptional.get()

        if (!usuarioModel.emFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Usuário não está em fila!"))

        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(usuarioModel.infoFila!!.codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido!"))
        val filaModel = filaModelOptional.get()

        if (!filaModel.chamarCliente)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Estabelecimento não chamou cliente!"))

        var primeiroFila = false

        if ((usuarioModel.infoFila!!.tipoFila == "prioridade" && filaModel.filaPrioridade.first() == usuarioModel.id)
            || (filaModel.filaPrioridade.size == 0 && usuarioModel.infoFila!!.tipoFila == "principal" && filaModel.filaPrincipal.first() == usuarioModel.id))
            primeiroFila = true

        if (!primeiroFila)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Usuário não é o primeiro da fila!"))

        filaModel.clienteConfirmouPresenca = true
        filaService.save(filaModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Presença confirmada!"))
    }

    // Apenas para debug
    @GetMapping("/{codigoFila}")
    @ApiOperation(value = "Retorna a lista de id de uma fila principal")
    @ApiResponses(
        ApiResponse(code = 200, message = "Lista de usuários na fila", response = UsuarioModel::class, responseContainer = "List"),
    )
    fun getFila(
        @PathVariable (value = "codigoFila") codigoFila: String,
    ): ResponseEntity<Any> {
        val filaModelOptional: Optional<FilaModel> = filaService.findByCode(codigoFila)
        if (!filaModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensagemPadraoDTO("Código inválido!"))

        return ResponseEntity.status(HttpStatus.OK).body(filaModelOptional.get().filaPrincipal)
    }

    // Modifica informações do usuário para indicar que não está em uma fila
    private fun removerUsuarioFila (idUsuario: UUID) : Unit  {
        val usuarioModel = usuarioService.findById(idUsuario).get()

        usuarioModel.emFila = false

        val usuarioInfoFilaModel = usuarioModel.infoFila!!
        usuarioModel.infoFila = null
        usuarioInfoFilaService.delete(usuarioInfoFilaModel)

        usuarioService.save(usuarioModel)
    }
}