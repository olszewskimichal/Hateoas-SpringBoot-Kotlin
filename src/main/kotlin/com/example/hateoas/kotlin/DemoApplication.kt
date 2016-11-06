package com.example.hateoas.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.stream.Stream
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
open class ExampleApplication @Autowired constructor(val customerRepository: CustomerRepository, val ordersRepository: OrdersRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        customerRepository.deleteAll()
        ordersRepository.deleteAll()
        Stream.of("Adam,NMG", "Bozena,Allegro", "Michał,Opel", "Arek,WKS")
                .map { v -> v.split(",") }.forEach { tpl -> customerRepository.save(Customer(tpl[0], tpl[1])) }
        customerRepository.findAll().forEach(::println)
        ordersRepository.save(Orders(2, BigDecimal.TEN, 1))
        ordersRepository.save(Orders(3, BigDecimal.valueOf(5), 2))
        ordersRepository.save(Orders(4, BigDecimal.valueOf(6), 3))
        ordersRepository.save(Orders(5, BigDecimal.valueOf(8), 4))
        ordersRepository.save(Orders(6, BigDecimal.valueOf(10), 1))
        ordersRepository.save(Orders(7, BigDecimal.valueOf(12), 2))
        ordersRepository.findAll().forEach(::println)
    }

    @Bean open fun objectMapperBuilder(): Jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())

}

fun main(args: Array<String>) {
    SpringApplication.run(ExampleApplication::class.java, *args)
}

open class Greeting @JsonCreator constructor(@JsonProperty("content") val content: String) : ResourceSupport()

@RestController open class GreetingController {

    @RequestMapping("/greeting")
    open fun greeting(@RequestParam(value = "name", required = false, defaultValue = "World") name: String): HttpEntity<Greeting> {
        val greeting = Greeting(String.format(TEMPLATE, name))
        greeting.add(linkTo(methodOn(GreetingController::class.java).greeting(name)).withSelfRel())
        return ResponseEntity<Greeting>(greeting, HttpStatus.OK)
    }

    companion object {
        private val TEMPLATE = "Hello, %s!"
    }
}

@Entity data class Customer(var name: String = "", var companyName: String = "", @Id @GeneratedValue var id: Long = 0)

@Entity data class Orders(var quantity: Int = 0, var price: BigDecimal = BigDecimal.ZERO, var customerId: Long = 0, @Id @GeneratedValue var id: Long = 0)

@Repository interface CustomerRepository : JpaRepository<Customer, Long>

@Repository interface OrdersRepository : JpaRepository<Orders, Long> {

    fun findOrdersByCustomerId(customerId: Long): MutableList<Orders>
}

open class CustomerResource @JsonCreator constructor(@JsonProperty("name") val name: String,@JsonProperty("companyName") val companyName: String, val id: Long) : ResourceSupport()

open class OrdersResource @JsonCreator constructor(val quantity: Int, val price: BigDecimal, val id: Long, val customerId: Long) : ResourceSupport()

@Service open class CustomerService constructor(var repository: CustomerRepository, var ordersRepository: OrdersRepository) {
    open fun getCustomerResourceById(id: Long): CustomerResource {
        val customer = repository.findOne(id)
        val customerResource = CustomerResource(customer.name, customer.companyName, customer.id)
        val withSelfRel = linkTo(CustomerController::class.java).slash(customer.id).withSelfRel()
        customerResource.add(withSelfRel)
        val findOrdersByCustomerId = ordersRepository.findOrdersByCustomerId(customer.id)
        for (value in findOrdersByCustomerId) {
            val orderRel = linkTo(OrderController::class.java).slash(value.id).withRel("Orders")
            customerResource.add(orderRel)
        }
        return customerResource
    }

    open fun getAllCustomerResource(): MutableList<CustomerResource> {
        return repository.findAll().map { v -> CustomerResource(v.name, v.companyName, v.id) }.toMutableList()
    }
}

@Service open class OrdersService constructor(var repository: OrdersRepository) {
    open fun getOrderById(id: Long): OrdersResource {
        val order = repository.findOne(id)
        val orderResource = OrdersResource(order.quantity, order.price, order.id, order.customerId)
        val withSelfRel = linkTo(OrderController::class.java).slash(order.id).withSelfRel()
        orderResource.add(withSelfRel)
        val orderBy = linkTo(CustomerController::class.java).slash(order.customerId).withRel("orderBy")
        orderResource.add(orderBy)
        return orderResource
    }

    open fun getAllOrders(): MutableList<OrdersResource> {
        return repository.findAll().map { v -> OrdersResource(v.quantity, v.price, v.id, v.customerId) }.toMutableList()
    }

}

@RestController @RequestMapping(value = "/orders") open class OrderController constructor(val service: OrdersService) {
    @GetMapping(value = "/{id}")
    fun getOrderById(@PathVariable id: Long): HttpEntity<OrdersResource> {
        val orderResource = service.getOrderById(id)
        val allCustomerLink = linkTo(OrderController::class.java).withRel("AllOrders")
        orderResource.add(allCustomerLink)
        return ResponseEntity<OrdersResource>(orderResource, HttpStatus.OK)
    }

    @GetMapping()
    fun getAllOrders(): ResponseEntity<List<OrdersResource>> {
        val orderResources = service.getAllOrders()
        for (a in orderResources) {
            val withSelfRel = linkTo(OrderController::class.java).slash(a.id).withSelfRel()
            a.add(withSelfRel)
        }
        return ResponseEntity(orderResources, HttpStatus.OK)
    }

}

@RestController @RequestMapping(value = "/customers") open class CustomerController constructor(val service: CustomerService) {

    @GetMapping(value = "/{id}")
    fun getCustomerById(@PathVariable id: Long): HttpEntity<CustomerResource> {
        val customerResourceById = service.getCustomerResourceById(id)
        val allCustomerLink = linkTo(CustomerController::class.java).withRel("AllCustomers")
        customerResourceById.add(allCustomerLink)
        return ResponseEntity<CustomerResource>(customerResourceById, HttpStatus.OK)
    }

    @GetMapping()
    fun getAllCustomers(): ResponseEntity<List<CustomerResource>> {
        val allCustomerResource = service.getAllCustomerResource()
        for (a in allCustomerResource) {
            val withSelfRel = linkTo(CustomerController::class.java).slash(a.id).withSelfRel()
            a.add(withSelfRel)
        }
        return ResponseEntity(allCustomerResource, HttpStatus.OK)

    }
}

