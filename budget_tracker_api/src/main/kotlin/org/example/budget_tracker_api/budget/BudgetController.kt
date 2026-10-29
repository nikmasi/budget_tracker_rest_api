package org.example.budget_tracker_api.budget

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/budgets")
class BudgetController(
    private val budgetService: BudgetService
) {

    @Operation(
        summary = "Set or update a budget",
        description = "Creates a budget or updates the existing budget for the specified month and category."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Budget created or updated successfully",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BudgetResponse::class))
                ]),
            ApiResponse(responseCode = "400", description = "Invalid month or amount", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Category not found", content = [Content()])
        ]
    )
    @PutMapping
    fun setBudget(
        @Valid @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated category name and color",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BudgetRequest::class)
                )
            ]
        )
        request: BudgetRequest
    ): BudgetResponse {
        return budgetService.setBudget(request)
    }

    @Operation(
        summary = "Get budgets for a month",
        description = "Returns all budgets for the specified month, sorted by category name."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Budgets retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = BudgetResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid month format", content = [Content()])
        ]
    )
    @GetMapping
    fun getBudgets(
        @Parameter(
            description = "Month for which budgets are requested, in YYYY-MM format.",
            example = "2026-10",
            required = true,
            schema = Schema(type = "string", pattern = "^\\d{4}-(0[1-9]|1[0-2])$")
        )
        @RequestParam month: String
    ): List<BudgetResponse> {
        return budgetService.getBudgets(month)
    }

    @Operation(
        summary = "Delete a budget",
        description = "Deletes the budget associated with the specified month and category."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            ApiResponse(responseCode = "400", description = "Invalid month format", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Budget not found", content = [Content()])
        ]
    )
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBudget(
        @Parameter(
            description = "Month of the budget to delete, in YYYY-MM format.", example = "2026-10", required = true
        )
        @RequestParam month: String,

        @Parameter(
            description = "ID of the category whose budget should be deleted.", example = "4", required = true
        )
        @RequestParam categoryId: Long
    ) {
        budgetService.deleteBudget(month, categoryId)
    }
}