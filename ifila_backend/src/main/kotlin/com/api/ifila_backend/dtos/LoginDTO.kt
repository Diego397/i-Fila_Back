package com.api.ifila_backend.dtos

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class LoginDTO (
    @ApiModelProperty(example = "example@gmail.com")
    val email: String,

    val senha: String
)