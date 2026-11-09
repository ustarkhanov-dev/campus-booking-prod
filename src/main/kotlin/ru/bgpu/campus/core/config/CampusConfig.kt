package ru.bgpu.campus.core.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.RestClient

@Configuration
class CampusConfig {

    @Bean
    fun restClient(): RestClient {
        return RestClient.create()
    }

    @Bean
    fun restTemplate(
        @Value("\${app.integration.bgpu.connect-timeout-ms:3000}") connectTimeout: Int,
        @Value("\${app.integration.bgpu.read-timeout-ms:5000}") readTimeout: Int
    ): RestTemplate {

        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(connectTimeout)
            setReadTimeout(readTimeout)
        }


        return RestTemplate(factory)
    }
}

