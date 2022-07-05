package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.*
import com.api.ifila_backend.models.FilaModel
import com.api.ifila_backend.models.UsuarioInfoFilaModel
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.EstabelecimentoService
import com.api.ifila_backend.services.FilaService
import com.api.ifila_backend.services.UsuarioInfoFilaService
import com.api.ifila_backend.services.UsuarioService
import com.api.ifila_backend.utils.FilaUtils
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
                      usuarioService: UsuarioService,
                      val filaUtils: FilaUtils) : BaseController(usuarioService) {

    // Rotas que podem ser acessadas pelos estabelecimentos

    @PostMapping("/abrir")
    @ApiOperation(value = "Abrir fila do estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Fila Aberta", response = MensagemPadraoDTO::class),
        ApiResponse(code = 400, message = "Fila já está aberta", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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
        filaModel.qtdUsuariosAtendidosPrincipal = 0
        filaModel.qtdUsuariosAtendidosPrioridade = 0
        filaModel.tempoMedioPrincipal = 1
        filaModel.tempoMedioPrioridade = 1

        estabelecimentoService.save(estabelecimentoModel)

        return ResponseEntity.status(HttpStatus.OK).body(MensagemPadraoDTO("Fila aberta!"))
    }

    @PutMapping("/fechar")
    @ApiOperation(value = "Fecha fila de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Fila fechada", response = FecharFilaReturnDTO::class),
        ApiResponse(code = 400, message = "Fila não está aberta", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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

        return ResponseEntity.status(HttpStatus.OK).body(FecharFilaReturnDTO(
            qtdUsuariosAtendidiosPrincipal = filaModel.qtdUsuariosAtendidosPrincipal,
            qtdUsuariosAtendidiosPrioridade = filaModel.qtdUsuariosAtendidosPrioridade,
            tempoMedioPrincipal = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrincipal),
            tempoMedioPrioridade = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrioridade)
        ))
    }

    @GetMapping("/info")
    @ApiOperation(value = "Retorna as informações da fila atual do estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações da fila do estabelecimento", response = InfoFilaEstabelecimentoDTO::class),
        ApiResponse(code = 400, message = "Fila não está aberta", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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
            tempoMedioPrincipal = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrincipal),
            tempoMedioPrioridade = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrioridade),
            horarioMaximoEntrada = filaModel.horarioMaximo!!,
            chamarCliente = filaModel.chamarCliente,
            clienteConfirmouPresenca = filaModel.clienteConfirmouPresenca
        ))
    }

    @PutMapping("/chamarcliente")
    @ApiOperation(value = "Solicita presença do próximo cliente")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações do cliente chamado", response = UsuarioModel::class),
        ApiResponse(code = 400, message = "Fila vazia | presença já solicitada", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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

        if (filaModel.filaPrincipal.isEmpty() && filaModel.filaPrioridade.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Não há nenhum usuario na fila!"))

        if (filaModel.chamarCliente)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Estabelecimento já solicitou a presença do cliente!"))

        var idPrimeiro:UUID? = null

        if(filaModel.ultimoPrioridade) // O ultimo chamado foi prioridade
            if (!filaModel.filaPrincipal.isEmpty())
                idPrimeiro = filaModel.filaPrincipal[0]
            else
                idPrimeiro = filaModel.filaPrioridade[0]
        else // O ultimo chamado foi normal
            if (!filaModel.filaPrioridade.isEmpty())
                idPrimeiro = filaModel.filaPrioridade[0]
            else
                idPrimeiro = filaModel.filaPrincipal[0]

        val usuarioFila = usuarioService.findByIdOrNull(idPrimeiro)

        usuarioFila!!.infoFila!!.presencaSolicitada = true
        usuarioService.save(usuarioFila)

        filaModel.chamarCliente = true
        filaService.save(filaModel)

        return ResponseEntity.status(HttpStatus.OK).body(AtenderClienteDTO(
            id = usuarioFila.id,
            nome = usuarioFila.nome,
            dataDeNascimento = usuarioFila.dataDeNascimento,
            email = usuarioFila.email,
            numeroCelular = usuarioFila.numeroCelular,
            cpf = usuarioFila.cpf,
            mensagem = "Usuario Chamado!",
            tipoFila = usuarioFila.infoFila!!.tipoFila
        ))
    }

    @PutMapping("/atendercliente")
    @ApiOperation(value = "Atende ou pula cliente na fila")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações do cliente atendido", response = AtenderClienteDTO::class),
        ApiResponse(code = 400, message = "Fila vazia/fechada | Cliente não confirmou presença (atender)", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('estabelecimento')")
    fun atenderCliente(
        @ApiParam(name = "pular", value = "0 - 1")
        @RequestParam(name="pular", defaultValue = "0", required = false) pular: String,
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

        if (tamanhoFilaPrincipal == 0 && tamanhoFilaPrioridade == 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Não há nenhum usuario na fila!"))

        if (!filaModel.clienteConfirmouPresenca && pular == "0")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Cliente não confirmou a presença!"))

        if (!filaModel.chamarCliente)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Cliente não chamado!"))

        var idPrimeiro:UUID? = null
        var filaPrincipal = true

        if ((filaModel.ultimoPrioridade && filaModel.filaPrincipal.isEmpty()) || (!filaModel.ultimoPrioridade && filaModel.filaPrioridade.isNotEmpty())) {
            idPrimeiro = filaModel.filaPrioridade[0]
            filaModel.ultimoPrioridade = true
            filaModel.filaPrioridade.removeFirst()
            filaPrincipal = false
            if (pular == "0")
                filaModel.qtdUsuariosAtendidosPrioridade += 1
        } else {
            idPrimeiro = filaModel.filaPrincipal[0]
            filaModel.ultimoPrioridade = false
            filaModel.filaPrincipal.removeFirst()
            filaModel.qtdUsuariosAtendidosPrincipal += 1
        }

        val usuarioFila = usuarioService.findByIdOrNull(idPrimeiro)!!

        var horarioSaida: LocalDateTime = LocalDateTime.now(ZoneId.of("America/Fortaleza"))

        val duracao = ChronoUnit.SECONDS.between(usuarioFila.infoFila!!.horarioEntrada, horarioSaida)
        if (filaPrincipal) {
            val segundosTotais = filaModel.qtdUsuariosAtendidosPrincipal * filaModel.tempoMedioPrincipal
            if (pular == "0")
                filaModel.qtdUsuariosAtendidosPrincipal += 1

            val novaMedia = (segundosTotais + duracao) / filaModel.qtdUsuariosAtendidosPrincipal

            filaModel.tempoMedioPrincipal = novaMedia
        } else {
            val segundosTotais = filaModel.qtdUsuariosAtendidosPrioridade * filaModel.tempoMedioPrioridade
            if (pular == "0")
                filaModel.qtdUsuariosAtendidosPrioridade += 1

            val novaMedia = (segundosTotais + duracao) / filaModel.qtdUsuariosAtendidosPrioridade

            filaModel.tempoMedioPrioridade = novaMedia
        }

        var tipoFila:String = usuarioFila.infoFila!!.tipoFila
        removerUsuarioFila(idPrimeiro)
        filaModel.chamarCliente = false
        filaModel.clienteConfirmouPresenca = false

        filaService.save(filaModel)

        var mensagem = ""
        if (pular == "1") // Cliente Pulado
            mensagem = "O Cliente foi Pulado!"
        else {
            mensagem = "O Cliente foi Atendido"
        }

        return ResponseEntity.status(HttpStatus.OK).body(AtenderClienteDTO(
            id = usuarioFila.id,
            nome = usuarioFila.nome,
            dataDeNascimento = usuarioFila.dataDeNascimento,
            email = usuarioFila.email,
            numeroCelular = usuarioFila.numeroCelular,
            cpf = usuarioFila.cpf,
            mensagem = mensagem,
            tipoFila = tipoFila
        ))
    }


    // Rotas que podem ser acessadas pelos usuários

    @PutMapping("/entrar/{codigoFila}")
    @ApiOperation(value = "Entra na fila de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações da posição do cliente na fila", response = InfoPosicaoFilaDTO::class),
        ApiResponse(code = 400, message = "Fila fechada | Capacidade/horário máximo atingido", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
    )
    @PreAuthorize("hasRole('usuario')")
    fun entrarFila(
        @PathVariable (value = "codigoFila") codigoFila: String,
        @RequestParam(name="tipoFila", defaultValue = "principal", required = false) tipoFila: String,
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
        val tempoMedioLong: Long
        if (tipoFila == "prioridade") {
            filaModel.filaPrioridade.add(usuarioModel.id!!)
            posicaoCliente = filaModel.filaPrioridade.size
            tempoMedioLong = filaModel.tempoMedioPrioridade
        } else {
            filaModel.filaPrincipal.add(usuarioModel.id!!)
            posicaoCliente = filaModel.filaPrincipal.size
            tempoMedioLong = filaModel.tempoMedioPrincipal
        }

        filaService.save(filaModel)

        usuarioModel.emFila = true
        usuarioModel.infoFila = UsuarioInfoFilaModel(
            tipoFila = tipoFila, codigoFila = codigoFila
        )

        usuarioService.save(usuarioModel)

        val tempoMedio = filaUtils.calcularTempoMedio(tempoMedioLong)

        return ResponseEntity.status(HttpStatus.OK).body(InfoPosicaoFilaDTO(
            posicao = posicaoCliente,
            deveConfirmarPresenca = false,
            tempoMedio = tempoMedio,
        ))
    }

    @GetMapping("/infoposicao")
    @ApiOperation(value = "Retorna as informações da fila atual do usuário")
    @ApiResponses(
        ApiResponse(code = 200, message = "Informações da posição do cliente na fila", response = InfoPosicaoFilaDTO::class),
        ApiResponse(code = 400, message = "Usuário não está em fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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
            posicaoCliente = filaModel.filaPrincipal.indexOf(usuarioModel.id) + 1

        var tempoMedio: LocalTime
        if (usuarioModel.infoFila!!.tipoFila == "principal")
            tempoMedio = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrincipal)
        else
            tempoMedio = filaUtils.calcularTempoMedio(filaModel.tempoMedioPrioridade)

        return ResponseEntity.status(HttpStatus.OK).body(InfoPosicaoFilaDTO(
            posicao = posicaoCliente,
            deveConfirmarPresenca = false,
            tempoMedio = tempoMedio,
        ))
    }

    @PutMapping("/sair")
    @ApiOperation(value = "Sair da fila de um estabelecimento")
    @ApiResponses(
        ApiResponse(code = 200, message = "Usuário saiu da fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 400, message = "Usuário não está em fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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
        ApiResponse(code = 200, message = "Usuário saiu da fila", response = MensagemPadraoDTO::class),
        ApiResponse(code = 400, message = "Usuário não está em fila | Presença não solicitada", response = MensagemPadraoDTO::class),
        ApiResponse(code = 404, message = "Recurso não encontrado", response = MensagemPadraoDTO::class)
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

        if (!usuarioModel.infoFila!!.presencaSolicitada)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MensagemPadraoDTO("Presença não solicitada!"))

        filaModel.clienteConfirmouPresenca = true
        filaModel.chamarCliente = false
        filaService.save(filaModel)

        usuarioModel.infoFila!!.presencaSolicitada = false
        usuarioService.save(usuarioModel)

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