package com.api.ifila_backend.configurations

import com.google.common.base.Predicates
import org.hibernate.usertype.DynamicParameterizedType.ParameterType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@Configuration
@EnableSwagger2
class SwaggerConfiguracao {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.api.ifila_backend.controllers"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(informacaoApi())
            .useDefaultResponseMessages(false)
            .securitySchemes(arrayListOf(apiKey()))
            .securityContexts(listSecurityContext())
            .directModelSubstitute(LocalTime::class.java, String::class.java)
    }

    private fun informacaoApi(): ApiInfo {
        return ApiInfoBuilder()
            .title("i.fila API")
            .description("API para comunicação com o backend do aplicativo i.fila")
            .version("1.0.0")
            .build()
    }

    private fun apiKey(): ApiKey {
        return ApiKey("JWT", "Authorization", "header")
    }


    private val metodosUsuario: List<HttpMethod> = Arrays.asList(
        HttpMethod.GET,
        HttpMethod.PUT,
        HttpMethod.DELETE
    )

    private fun listSecurityContext(): List<SecurityContext> {
        val listaSeguranca: MutableList<SecurityContext> = ArrayList()

        listaSeguranca.add(
            SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant("/usuarios/**"))
                .forHttpMethods(Predicates.`in`(metodosUsuario))
                .build()
        )

        listaSeguranca.add(
            SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant("/estabelecimentos/**"))
                .build()
        )

        return listaSeguranca
    }

    private fun defaultAuth(): List<SecurityReference?>? {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(SecurityReference("JWT", authorizationScopes))
    }
}