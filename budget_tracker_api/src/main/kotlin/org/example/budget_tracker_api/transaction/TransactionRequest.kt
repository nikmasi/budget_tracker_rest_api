package org.example.budget_tracker_api.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionRequest(
    @field:NotNull(message = "Type is required")
    val type: TransactionType,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin("0.01")
    @field:DecimalMax("1000000.00")
    @field:Positive(message = "Amount must be greater than zero")
    @field:Digits(
        integer = 7,
        fraction = 2,
        message = "Amount must have at most two decimal places"
    )
    val amount: BigDecimal,

    @field:NotNull(message = "Date is required")
    val date: LocalDate,

    @field:Size(max = 200)
    val description: String? = null,

    @field:NotNull(message = "Category ID is required")
    @field:Positive(message = "Category ID must be positive")
    @field:JsonProperty("category_id")
    val categoryId: Long
)