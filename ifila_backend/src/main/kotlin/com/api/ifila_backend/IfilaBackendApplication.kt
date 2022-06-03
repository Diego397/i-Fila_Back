package com.api.ifila_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class IfilaBackendApplication()

fun main(args: Array<String>) {
	runApplication<IfilaBackendApplication>(*args)
}
