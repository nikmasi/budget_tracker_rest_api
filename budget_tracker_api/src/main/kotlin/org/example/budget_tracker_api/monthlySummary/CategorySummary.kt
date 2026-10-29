package org.example.budget_tracker_api.monthlySummary

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(
    description = "Summary of spending and budget for a single category. All amounts are in euros (EUR)."
)
data class CategorySummary(

    @field:Schema(
        description = "Name of the category.", example = "Food"
    )
    val categoryName: String,

    @field:Schema(
        description = "Total amount spent in this category, in euros (EUR).", example = "250.50"
    )
    val spent: BigDecimal,

    @field:Schema(
        description = "Budget allocated to this category, in euros (EUR). Zero if no budget is set.", example = "300.00"
    )
    val budget: BigDecimal,

    @field:Schema(
        description = "Remaining budget, calculated as budget minus spent, in euros (EUR). Can be negative if spending exceeds the budget.",
        example = "49.50"
    )
    val remaining: BigDecimal
)