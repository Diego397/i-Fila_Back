package com.api.ifila_backend.repositories

import com.api.ifila_backend.models.FilaModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FilaRepository: JpaRepository<FilaModel, UUID> {

}