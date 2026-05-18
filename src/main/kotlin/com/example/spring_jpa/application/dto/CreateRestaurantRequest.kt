package com.example.spring_jpa.application.dto

import jakarta.validation.constraints.NotBlank


data class CreateRestaurantRequest(

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Address cannot be blank")
    val address: String

)
