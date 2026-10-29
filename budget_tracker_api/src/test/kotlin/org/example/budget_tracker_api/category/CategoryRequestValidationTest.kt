package org.example.budget_tracker_api.category

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CategoryRequestValidationTest {

    private val validator =
        Validation.buildDefaultValidatorFactory().validator

    private fun request(
        name: String,
        color: String = "#FF0000"
    ) = CategoryRequest(
        name = name,
        color = color
    )

    @Test
    fun emptyNameIsRejected() {
        assertTrue(
            validator.validate(request("")).any {
                it.propertyPath.toString() == "name"
            }
        )
    }

    @Test
    fun whitespaceNameIsRejected() {
        assertTrue(
            validator.validate(request("   ")).any {
                it.propertyPath.toString() == "name"
            }
        )
    }

    @Test
    fun oneCharacterNameIsAccepted() {
        assertTrue(validator.validate(request("A")).isEmpty())
    }

    @Test
    fun thirtyCharacterNameIsAccepted() {
        assertTrue(
            validator.validate(request("A".repeat(30))).isEmpty()
        )
    }

    @Test
    fun thirtyOneCharacterNameIsRejected() {
        assertTrue(
            validator.validate(request("A".repeat(31))).any {
                it.propertyPath.toString() == "name"
            }
        )
    }

    @Test
    fun validHexColorIsAccepted() {
        assertTrue(
            validator.validate(request("Food", "#FF5733")).isEmpty()
        )
    }

    @Test
    fun colorWithoutHashIsRejected() {
        assertTrue(
            validator.validate(request("Food", "FF5733")).any {
                it.propertyPath.toString() == "color"
            }
        )
    }

    @Test
    fun colorWithInvalidCharactersIsRejected() {
        assertTrue(
            validator.validate(request("Food", "#GG5733")).any {
                it.propertyPath.toString() == "color"
            }
        )
    }

    @Test
    fun colorWithWrongLengthIsRejected() {
        assertTrue(
            validator.validate(request("Food", "#FF573")).any {
                it.propertyPath.toString() == "color"
            }
        )
    }
}