package com.api.ifila_backend.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ResponseMessage
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

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
    }

    private fun informacaoApi(): ApiInfo {
        return ApiInfoBuilder()
            .title("i.fila API")
            .description("API para comunicação com o backend do aplicativo i.fila")
            .version("1.0.0")
            .build()
    }
}