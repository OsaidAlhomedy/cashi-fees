package com.cashi.fees.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun feesOpenApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("Cashi Fees API")
            .version("0.1.0")
            .description("Calculates, charges and records transaction fees via a Restate workflow.")
    )
}