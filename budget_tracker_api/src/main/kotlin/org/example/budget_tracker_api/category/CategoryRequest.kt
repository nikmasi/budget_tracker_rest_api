package org.example.budget_tracker_api.category

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Data required to create or update a category")
data class CategoryRequest(

    @field:Schema(
        description = "Category name. Required, 1–30 characters. " +
                "Leading and trailing spaces are removed. " +
                "Must be unique, ignoring case.",
        example = "Eating out",
        minLength = 1,
        maxLength = 30,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 30, message = "Category name must not exceed 30 characters")
    val name: String,

    @field:Schema(
        description = "Category color in hexadecimal #RRGGBB format. Required.",
        example = "#FF5733",
        pattern = "^#[0-9A-Fa-f]{6}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "Category color is required")
    @field:Pattern(
        regexp = "^#[0-9A-Fa-f]{6}$",
        message = "Color must be in #RRGGBB format"
    )
    val color: String
)