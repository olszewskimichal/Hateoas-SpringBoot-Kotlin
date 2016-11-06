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
import java.math.BigDecimal


class OrdersMockMvcTests {

    lateinit var mockMvc: MockMvc

    @InjectMocks
    lateinit var controller: OrderController

    @Mock
    lateinit var service: OrdersService

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(MappingJackson2HttpMessageConverter()).build()
    }

    @Test
    fun getAllOrders() {
        Mockito.`when`(service.getAllOrders()).thenReturn(mutableListOf(OrdersResource(3, BigDecimal.TEN, 1, 1)))

        mockMvc.perform(get("/orders"))
                .andDo { print() }
                .andExpect { status().isOk }
                .andExpect(jsonPath("$[0].quantity").value(3))
                .andExpect(jsonPath("$[0].price").value(BigDecimal.TEN))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].links[*].href", hasItem(("http://localhost/orders/1"))))
    }

    @Test
    fun getOrderById() {
        Mockito.`when`(service.getOrderById(1)).thenReturn((OrdersResource(3, BigDecimal.TEN, 1, 1)))

        mockMvc.perform(get("/orders/1"))
                .andDo { print() }
                .andExpect { status().isOk }
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.price").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.links[*].href", hasItem(("http://localhost/orders"))))

    }
}