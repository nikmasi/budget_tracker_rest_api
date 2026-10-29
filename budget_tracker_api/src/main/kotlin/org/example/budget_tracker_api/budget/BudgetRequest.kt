package org.example.budget_tracker_api.budget

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@Schema(description = "Data required to create or update a monthly budget")
data class BudgetRequest(

    @field:Schema(
        description = "Budget month in YYYY-MM format",
        example = "2026-10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:Pattern(
        regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
        message = "Month must be in YYYY-MM format"
    )
    @field:NotBlank(message = "Month is required")
    val month: String,

    @field:Schema(
        description = "ID of an existing category",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "Category ID is required")
    @field:Positive(message = "Category ID must be positive")
    val categoryId: Long,

    @field:Schema(
        description = "Budget amount, greater than zero",
        example = "500.00",
        minimum = "0.01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "Budget amount is required")
    @field:Positive(message = "Amount must be greater than zero")
    @field:DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    @field:DecimalMax(value = "1000000.00", message = "Budget amount must not exceed 1,000,000.00")
    @field:Digits(
        integer = 7,
        fraction = 2,
        message = "Amount must have at most two decimal places"
    )
    val amount: BigDecimal
)