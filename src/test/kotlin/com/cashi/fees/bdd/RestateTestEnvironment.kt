package com.cashi.fees.bdd

import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object RestateTestEnvironment {
    val sdkPort: Int = ServerSocket(0).use { it.localPort }
    private val container: GenericContainer<*>

    init {
        Testcontainers.exposeHostPorts(sdkPort)
        container = GenericContainer("docker.restate.dev/restatedev/restate:latest")
            .withExposedPorts(8080, 9070)
            .waitingFor(
                WaitAllStrategy()
                    .withStrategy(Wait.forHttp("/restate/health").forPort(8080))
                    .withStrategy(Wait.forHttp("/health").forPort(9070))
            )
        container.start()
    }

    val ingressUrl: String get() = "http://localhost:${container.getMappedPort(8080)}"
    val adminUrl: String get() = "http://localhost:${container.getMappedPort(9070)}"


    private val registered = AtomicBoolean(false)

    fun registerDeployment() {
        if (!registered.compareAndSet(expectedValue = false, newValue = true)) return
        val body = """{"uri":"http://host.testcontainers.internal:$sdkPort","force":true}"""
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("$adminUrl/deployments"))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        check(response.statusCode() in 200..299) {
            "deployment registration failed: ${response.statusCode()} ${response.body()}"
        }
    }
}