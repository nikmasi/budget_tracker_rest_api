package org.example.budget_tracker_api.budget

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Budget
import org.example.budget_tracker_api.database.Category
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BudgetAmountValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun budget(amount: BigDecimal) = Budget(
        month = "2026-10",
        amount = amount,
        category = Category(
            name = "Food",
            color = "#FF0000"
        )
    )

    private fun isAmountValid(amount: BigDecimal): Boolean =
        validator.validate(budget(amount)).none {
            it.propertyPath.toString() == "amount"
        }

    private fun getAmountViolationMessage(amount: BigDecimal): String? =
        validator.validate(budget(amount)).firstOrNull { it.propertyPath.toString() == "amount" }?.message

    @Test
    fun zeroAmountRejected() {
        assertFalse(isAmountValid(BigDecimal("0.00")))
    }

    @Test
    fun negativeAmountRejected() {
        assertFalse(isAmountValid(BigDecimal("-10.00")))
    }

    @Test
    fun amountBelowMinimumRejected() {
        assertFalse(isAmountValid(BigDecimal("0.001")))
    }

    @Test
    fun amountAboveMaximumRejected() {
        assertFalse(isAmountValid(BigDecimal("1000000.01")))
    }

    @Test
    fun amountWithThreeDecimalPlacesRejected() {
        assertFalse(isAmountValid(BigDecimal("10.555")))
    }

    @Test
    fun positiveAmountAccepted() {
        assertTrue(isAmountValid(BigDecimal("100.00")))
    }

    @Test
    fun minimumAmountAccepted() {
        assertTrue(isAmountValid(BigDecimal("0.01")))
    }

    @Test
    fun maximumAmountAccepted() {
        assertTrue(isAmountValid(BigDecimal("1000000.00")))
    }

    @Test
    fun amountWithOneDecimalPlaceAccepted() {
        assertTrue(isAmountValid(BigDecimal("10.5")))
    }

    @Test
    fun amountWithTwoDecimalPlacesAccepted() {
        assertTrue(isAmountValid(BigDecimal("10.55")))
    }

    @Test
    fun amountWithThreeDecimalPlacesRejectedWithCorrectMessage() {
        val message = getAmountViolationMessage(BigDecimal("10.555"))

        assertEquals("Amount must have at most 2 decimal places", message)
    }

    @Test
    fun zeroAmountRejectedWithCorrectMessage() {
        val message = getAmountViolationMessage(BigDecimal("0.00"))

        assertEquals("Amount must be greater than 0", message)
    }

    @Test
    fun amountAboveMaximumRejectedWithCorrectMessage() {
        val message = getAmountViolationMessage(BigDecimal("1000000.01"))

        assertEquals("Amount must be at most 1,000,000.00", message)
    }
}
