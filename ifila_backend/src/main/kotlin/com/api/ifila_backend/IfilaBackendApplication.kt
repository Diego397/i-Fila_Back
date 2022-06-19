package com.api.ifila_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class IfilaBackendApplication()

fun main(args: Array<String>) {
	runApplication<IfilaBackendApplication>(*args)
}
