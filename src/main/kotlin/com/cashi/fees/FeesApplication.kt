package com.cashi.fees

import dev.restate.sdk.springboot.EnableRestate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableRestate
class FeesApplication

fun main(args: Array<String>) {
    runApplication<FeesApplication>(*args)
}
