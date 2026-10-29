package org.example.budget_tracker_api.monthlySummary

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(
    description = "Monthly summary of income and expenses. All amounts are in euros (EUR)."
)
data class MonthlySummaryResponse(

    @field:Schema(
        description = "Month for which the summary is generated, in YYYY-MM format.", example = "2026-10"
    )
    val yearMonth: String,

    @field:Schema(
        description = "Total income for the month, in euros (EUR).", example = "2500.00"
    )
    val totalIncome: BigDecimal,

    @field:Schema(
        description = "Total expenses for the month, in euros (EUR).", example = "1250.50"
    )
    val totalExpense: BigDecimal,

    @field:Schema(
        description = "Monthly balance, calculated as totalIncome minus totalExpense, in euros (EUR).", example = "1249.50"
    )
    val balance: BigDecimal,

    @field:Schema(
        description = "Breakdown of income and expenses by category."
    )
    val categorySummaries: List<CategorySummary>
)