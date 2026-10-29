package org.example.budget_tracker_api.category

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Category
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryNameValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun category(name: String) = Category(
        name = name,
        color = "#FF0000"
    )

    @Test
    fun emptyNameIsRejected() {
        val violations = validator.validate(category(""))

        assertTrue(violations.any {
            it.propertyPath.toString() == "name"
        })
    }

    @Test
    fun whitespaceNameIsRejected() {
        val violations = validator.validate(category("   "))

        assertTrue(violations.any {
            it.propertyPath.toString() == "name"
        })
    }

    @Test
    fun oneCharacterNameAccepted() {
        assertTrue(validator.validate(category("A")).isEmpty())
    }

    @Test
    fun twoCharacterNameAccepted() {
        assertTrue(validator.validate(category("AB")).isEmpty())
    }

    @Test
    fun character29NameAccepted() {
        val name = "A".repeat(29)

        assertTrue(validator.validate(category(name)).isEmpty())
    }

    @Test
    fun character30NameAccepted() {
        val name = "A".repeat(30)

        assertTrue(validator.validate(category(name)).isEmpty())
    }

    @Test
    fun character31NameAccepted() {
        val name = "A".repeat(31)

        val violations = validator.validate(category(name))

        assertTrue(violations.any {
            it.propertyPath.toString() == "name"
        })
    }

    @Test
    fun leadingAndTrailingSpacesCanBeTrimmed() {
        val category = category("  Food  ")

        category.name = category.name.trim()

        assertEquals("Food", category.name)
        assertTrue(validator.validate(category).isEmpty())
    }

    @Test
    fun trimmedNameExceeding30charactersRejected() {
        val category = category("  ${"A".repeat(31)}  ")

        category.name = category.name.trim()

        assertTrue(validator.validate(category).any {
            it.propertyPath.toString() == "name"
        })
    }
}