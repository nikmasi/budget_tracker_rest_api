package org.example.budget_tracker_api.monthlySummary

import org.example.budget_tracker_api.budget.BudgetRepository
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.example.budget_tracker_api.transaction.TransactionType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class MonthlySummaryService(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    fun getMonthlySummary(year: Int, month: Int): MonthlySummaryResponse {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        val transactions = transactionRepository.findByDateBetween(startDate, endDate)

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome.subtract(totalExpense)

        val yearMonth = "$year-${String.format("%02d", month)}"

        val spentByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.name }
            .mapValues { (_, categoryTransactions) ->
                categoryTransactions.sumOf { it.amount }
            }

        val budgetsByCategory = budgetRepository.findAllByMonth(yearMonth)
            .associate { budget ->
                budget.category.name to budget.amount
            }

        val categoryNames = spentByCategory.keys + budgetsByCategory.keys

        val categorySummaries = categoryNames
            .distinct()
            .map { categoryName ->
                val spent = spentByCategory[categoryName] ?: BigDecimal.ZERO
                val budget = budgetsByCategory[categoryName] ?: BigDecimal.ZERO

                CategorySummary(
                    categoryName = categoryName,
                    spent = spent,
                    budget = budget,
                    remaining = budget.subtract(spent)
                )
            }
            .sortedWith(
                compareByDescending<CategorySummary> { it.spent }
                    .thenBy { it.categoryName.lowercase() }
            )

        return MonthlySummaryResponse(
            yearMonth = yearMonth,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            categorySummaries = categorySummaries
        )
    }
}