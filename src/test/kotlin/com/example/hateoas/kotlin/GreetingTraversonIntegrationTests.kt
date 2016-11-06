package com.example.hateoas.kotlin

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.client.Traverson
import java.net.URI

class GreetingTraversonIntegrationTests : DemoApplicationTests() {
    @LocalServerPort
    private val port: Int = 0

    @Test
    @Throws(Exception::class)
    fun envEndpointNotHidden() {
        val traverson = Traverson(URI("http://localhost:" + this.port + "/greeting"), MediaTypes.HAL_JSON)
        val greeting = traverson.follow("self").toObject<String>("$.content")
        assertThat(greeting).isEqualTo("Hello, World!")
    }
}