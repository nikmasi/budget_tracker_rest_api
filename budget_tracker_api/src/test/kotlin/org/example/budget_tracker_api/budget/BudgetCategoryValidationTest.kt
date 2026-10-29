package org.example.budget_tracker_api.budget

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Budget
import org.example.budget_tracker_api.database.Category
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BudgetCategoryValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun budget(category: Category?) = Budget(
        month = "2026-10",
        amount = BigDecimal("100.00"),
        category = category!!
    )

    private fun isCategoryValid(category: Category?): Boolean =
        category != null &&
                validator.validate(budget(category)).none {
                    it.propertyPath.toString() == "category"
                }

    @Test
    fun existingCategoryAccepted() {
        val category = Category(
            id = 1L,
            name = "Food",
            color = "#FF0000"
        )

        assertTrue(isCategoryValid(category))
    }

    @Test
    fun nullCategoryRejected() {
        assertFalse(isCategoryValid(null))
    }
}
