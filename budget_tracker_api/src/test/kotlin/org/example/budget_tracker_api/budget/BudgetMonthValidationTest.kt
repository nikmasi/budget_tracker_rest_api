package org.example.budget_tracker_api.budget

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Budget
import org.example.budget_tracker_api.database.Category
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BudgetMonthValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun budget(month: String) = Budget(
        month = month,
        amount = BigDecimal("100.00"),
        category = Category(
            name = "Food",
            color = "#FF0000"
        )
    )

    private fun isMonthValid(month: String): Boolean =
        validator.validate(budget(month)).none {
            it.propertyPath.toString() == "month"
        }

    private fun monthViolationMessages(month: String): Set<String> =
        validator.validate(budget(month))
            .filter { it.propertyPath.toString() == "month" }
            .map { it.message }
            .toSet()

    @Test
    fun emptyMonthRejected() {
        assertFalse(isMonthValid(""))
    }

    @Test
    fun blankMonthRejected() {
        assertFalse(isMonthValid(" "))
    }

    @Test
    fun monthWithoutHyphenRejected() {
        assertFalse(isMonthValid("202610"))
    }

    @Test
    fun monthWithSlashRejected() {
        assertFalse(isMonthValid("2026/10"))
    }

    @Test
    fun monthWithIncorrectFormatRejected() {
        assertFalse(isMonthValid("10-2026"))
    }

    @Test
    fun monthWithSingleDigitMonthRejected() {
        assertFalse(isMonthValid("2026-1"))
        assertTrue(monthViolationMessages("2026-1").contains("Month must be in format YYYY-MM"))
    }

    @Test
    fun monthWithExtraCharactersRejected() {
        assertFalse(isMonthValid("2026-10-extra"))
        assertTrue(monthViolationMessages("2026-10-extra").contains("Month must be in format YYYY-MM"))
    }

    @Test
    fun monthWithSpacesRejected() {
        assertFalse(isMonthValid(" 2026-10 "))
        assertTrue(monthViolationMessages(" 2026-10 ").contains("Month must be in format YYYY-MM"))
    }

    @Test
    fun monthZeroRejected() {
        assertFalse(isMonthValid("2026-00"))
        assertTrue(monthViolationMessages("2026-00").contains("Month must be in format YYYY-MM"))
    }

    @Test
    fun monthThirteenRejected() {
        assertFalse(isMonthValid("2026-13"))
        assertTrue(monthViolationMessages("2026-13").contains("Month must be in format YYYY-MM"))
    }

    @Test
    fun januaryAccepted() {
        assertTrue(isMonthValid("2026-01"))
    }

    @Test
    fun decemberAccepted() {
        assertTrue(isMonthValid("2026-12"))
    }

    @Test
    fun middleMonthAccepted() {
        assertTrue(isMonthValid("2026-06"))
    }

    @Test
    fun leapYearFebruaryAccepted() {
        assertTrue(isMonthValid("2024-02"))
    }
}
