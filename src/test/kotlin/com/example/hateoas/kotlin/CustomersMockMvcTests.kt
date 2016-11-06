package com.example.hateoas.kotlin

import org.hamcrest.Matchers.hasItem
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders


class CustomersMockMvcTests {

    lateinit var mockMvc: MockMvc

    @InjectMocks
    lateinit var controller: CustomerController

    @Mock
    lateinit var service: CustomerService

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(MappingJackson2HttpMessageConverter()).build()
    }

    @Test
    fun getAllCustomer() {
        Mockito.`when`(service.getAllCustomerResource()).thenReturn(mutableListOf(CustomerResource("Imie", "nazwisko", 1)))

        mockMvc.perform(get("/customers"))
                .andDo { print() }
                .andExpect { status().isOk }
                .andExpect(jsonPath("$[0].name").value("Imie"))
                .andExpect(jsonPath("$[0].companyName").value("nazwisko"))
                .andExpect(jsonPath("$[0].links[*].href", hasItem(("http://localhost/customers/1"))))
    }

    @Test
    fun getCustomerById() {
        Mockito.`when`(service.getCustomerResourceById(1)).thenReturn(CustomerResource("Imie", "nazwisko", 1))

        mockMvc.perform(get("/customers/1"))
                .andDo { print() }
                .andExpect { status().isOk }
                .andExpect(jsonPath("$.name").value("Imie"))
                .andExpect(jsonPath("$.companyName").value("nazwisko"))
                .andExpect(jsonPath("$.links[*].href", hasItem(("http://localhost/customers"))))
    }
}