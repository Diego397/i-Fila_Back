package com.api.ifila_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class IfilaBackendApplication()

fun main(args: Array<String>) {
	runApplication<IfilaBackendApplication>(*args)
}
