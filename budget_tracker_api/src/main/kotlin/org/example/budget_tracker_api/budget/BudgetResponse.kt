package org.example.budget_tracker_api.budget

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Monthly budget details returned by the API")
data class BudgetResponse(

    @field:Schema(description = "Unique budget identifier", example = "1")
    val id: Long,

    @field:Schema(description = "Budget month in YYYY-MM format", example = "2026-10")
    val month: String,

    @field:Schema(description = "ID of the associated category", example = "1")
    val categoryId: Long,

    @field:Schema(example = "Food", description = "Name of the associated category.")
    val categoryName: String,

    @field:Schema(description = "Budget amount", example = "500.00")
    val amount: BigDecimal
)