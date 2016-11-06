package com.example.hateoas.kotlin

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import java.util.*

class CustomersIntegrationTests : DemoApplicationTests() {
    @Autowired lateinit var restTemplate: TestRestTemplate

    @Autowired lateinit var repository:CustomerRepository


    @LocalServerPort
    private val port: Int = 0

    var id:Long=0

    @Before
    fun setup(){
        repository.deleteAll()
        val save = repository.save(Customer("imie", "firma"))
        println(save)
        id=save.id
    }

    @Test
    fun restTest(){
        var p=thenGetCustomerByIdFromApi()

        assertThat(p).isNotNull()
        assertThat(p.name).isEqualToIgnoringCase("imie")
        assertThat(p.companyName).isEqualToIgnoringCase("firma")
    }

    @Test
    fun restTest2(){
        var p=thenGetCustomersFromApi()

        assertThat(p).isNotNull
        assertThat(p.size).isEqualTo(1)
        assertThat(p[0].name).isEqualToIgnoringCase("imie")
        assertThat(p[0].companyName).isEqualToIgnoringCase("firma")
    }

    fun thenGetCustomerByIdFromApi():CustomerResource = restTemplate.getForEntity(
            "http://localhost:$port/customers/$id", CustomerResource::class.java).body

    fun thenGetCustomersFromApi(): ArrayList<CustomerResource> {
        val rateResponse = restTemplate.exchange("http://localhost:$port/customers",
                HttpMethod.GET, null, object : ParameterizedTypeReference<List<CustomerResource>>() {

        })
        return rateResponse.body as ArrayList<CustomerResource>
    }
}

