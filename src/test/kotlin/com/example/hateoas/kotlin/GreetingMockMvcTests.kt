package com.example.hateoas.kotlin

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


class GreetingMockMvcTests: DemoApplicationTests() {

    @Autowired lateinit var mockMvc:MockMvc

    @Test
    fun envEndpointNotHidden(){
        mockMvc.perform(
                MockMvcRequestBuilders.get("/greeting"))
                .andExpect { MockMvcResultMatchers.jsonPath("$.content").value("Hello, World!")  }

    }
}