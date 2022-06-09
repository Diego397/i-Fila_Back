package com.api.ifila_backend.controllers

import com.api.ifila_backend.dtos.UsuarioDTO
import com.api.ifila_backend.models.UsuarioModel
import com.api.ifila_backend.services.UsuarioService
import org.apache.coyote.Response
import org.springframework.beans.BeanUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/usuarios")
class UsuarioController (val usuarioService: UsuarioService){

    @PostMapping
    fun cadastrarUsuario(@RequestBody @Valid usuarioDTO: UsuarioDTO):ResponseEntity<Any>{

        when{
            usuarioService.existsByEmail(usuarioDTO.email) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "Email já cadastrado."))
            usuarioService.existsByCpf(usuarioDTO.cpf) -> return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "Cpf já cadastrado"))
        }

        val usuarioModel = UsuarioModel()
        BeanUtils.copyProperties(usuarioDTO, usuarioModel)

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(usuarioModel))
    }

    @GetMapping
    fun getUsuarios(): ResponseEntity<List<UsuarioModel>> {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.findAll())
    }

    @GetMapping("/{id}")
    fun getUsuario(@PathVariable (value = "id") id:UUID): ResponseEntity<Any> {

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)
        if(!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensagem" to "Usuário não encontrado!"))

        return ResponseEntity.status(HttpStatus.OK).body(usuarioModelOptional)
    }

    @PutMapping("/{id}")
    fun putUsuario(@PathVariable (value = "id") id:UUID,
                   @RequestBody @Valid usuarioDTO: UsuarioDTO): ResponseEntity<Any>{

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)

        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensagem" to "Usuário não encontrado!"))

        if(usuarioDTO.email != usuarioModelOptional.get().email && usuarioService.existsByEmail(usuarioDTO.email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "Email já cadastrado."))

        if (usuarioDTO.cpf != usuarioModelOptional.get().cpf && usuarioService.existsByCpf(usuarioDTO.cpf))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to "Cpf já cadastrado"))

        val usuarioModel = UsuarioModel()
        BeanUtils.copyProperties(usuarioDTO, usuarioModel)
        usuarioModel.id = usuarioModelOptional.get().id
        usuarioModel.dataDeCriacao = usuarioModelOptional.get().dataDeCriacao

        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.save(usuarioModel))
    }
    @DeleteMapping("/{id}")
    fun deleteUsuario(@PathVariable(value = "id") id: UUID): ResponseEntity<Any> {

        val usuarioModelOptional: Optional<UsuarioModel> = usuarioService.findById(id)

        if (!usuarioModelOptional.isPresent)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensagem" to "Usuário não encontrado!"))

        usuarioService.delete(usuarioModelOptional.get())

        return ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "Usuário removido com sucesso"))
    }
}