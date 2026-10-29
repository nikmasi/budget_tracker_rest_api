package org.example.budget_tracker_api.budget

import org.example.budget_tracker_api.category.CategoryRepository
import org.example.budget_tracker_api.database.Budget
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.YearMonth

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    fun setBudget(request: BudgetRequest): BudgetResponse {
        val month = validateMonth(request.month)
        validateAmount(request.amount)

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Category with id ${request.categoryId} not found."
                )
            }

        val existing = budgetRepository.findByMonthAndCategory_Id(month, request.categoryId)

        val budget = if (existing != null) {
            existing.amount = request.amount
            existing
        } else {
            Budget(month = month, category = category, amount = request.amount)
        }

        return budgetRepository.save(budget).toResponse()
    }

    @Transactional(readOnly = true)
    fun getBudgets(month: String): List<BudgetResponse> {
        val validMonth = validateMonth(month)

        return budgetRepository.findAllByMonthOrderByCategory_NameAsc(validMonth).map { it.toResponse() }
    }

    @Transactional
    fun deleteBudget(month: String, categoryId: Long) {
        val validMonth = validateMonth(month)

        val budget = budgetRepository.findByMonthAndCategory_Id(
            validMonth,
            categoryId
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Budget not found for category $categoryId and month $validMonth."
        )

        budgetRepository.delete(budget)
    }

    private fun validateMonth(month: String): String {
        val parsed = try {
            YearMonth.parse(month)
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Month must be in YYYY-MM format, for example 2026-10."
            )
        }

        if (parsed.toString() != month) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Month must be in YYYY-MM format, for example 2026-10."
            )
        }

        return month
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount < BigDecimal("0.01")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Amount must be greater than 0."
            )
        }

        if (amount > BigDecimal("1000000.00")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Amount must not exceed 1000000.00."
            )
        }

        if (amount.scale() > 2) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Amount must have at most 2 decimal places."
            )
        }
    }

    private fun Budget.toResponse(): BudgetResponse =
        BudgetResponse(
            id = requireNotNull(id),
            month = month,
            categoryId = requireNotNull(category.id),
            categoryName = category.name,
            amount = amount
        )
}