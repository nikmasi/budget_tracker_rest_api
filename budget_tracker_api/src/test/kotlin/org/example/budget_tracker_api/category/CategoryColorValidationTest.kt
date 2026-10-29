package org.example.budget_tracker_api.category

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Category
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryColorValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun category(color: String) = Category(
        name = "Food",
        color = color
    )

    private fun isColorValid(color: String): Boolean =
        validator.validate(category(color)).none {
            it.propertyPath.toString() == "color"
        }

    @Test
    fun emptyColorRejected() {
        assertFalse(isColorValid(""))
    }

    @Test
    fun colorWithoutHashRejected() {
        assertFalse(isColorValid("FF0000"))
    }

    @Test
    fun colorWithTooFewCharactersRejected() {
        assertFalse(isColorValid("#FFF"))
    }

    @Test
    fun colorWithTooManyCharactersRejected() {
        assertFalse(isColorValid("#FF000000"))
    }

    @Test
    fun colorWithInvalidHexCharactersRejected() {
        assertFalse(isColorValid("#GG0000"))
    }

    @Test
    fun colorWithHashAtTheEndRejected() {
        assertFalse(isColorValid("FF0000#"))
    }

    @Test
    fun blackIsAccepted() {
        assertTrue(isColorValid("#000000"))
    }

    @Test
    fun whiteIsAccepted() {
        assertTrue(isColorValid("#FFFFFF"))
    }

    @Test
    fun lowercaseHexColorAccepted() {
        assertTrue(isColorValid("#ff00aa"))
    }

    @Test
    fun mixedCaseHexColorAccepter() {
        assertTrue(isColorValid("#Ff00aA"))
    }

    @Test
    fun colorWithSpacesRejected() {
        assertFalse(isColorValid(" #FF0000 "))
    }
}