package com.cashi.fees.bdd

import dev.restate.client.Client
import dev.restate.sdk.springboot.RestateClientProperties
import dev.restate.serde.kotlinx.KotlinSerializationSerdeFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class RestateClientTestConfig {

    @Bean
    @Primary
    fun kotlinxRestateClient(props: RestateClientProperties): Client =
        Client.connect(props.baseUri, KotlinSerializationSerdeFactory())

}