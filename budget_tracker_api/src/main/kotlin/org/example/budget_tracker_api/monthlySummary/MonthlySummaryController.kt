package org.example.budget_tracker_api.monthlySummary

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transactions")
class MonthlySummaryController(
    private val monthlySummaryService: MonthlySummaryService
) {

    @Operation(
        summary = "Get monthly transaction summary",
        description = "Returns a summary of income, expenses, and balance for the specified year and month."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Monthly summary retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = MonthlySummaryResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid year or month parameter",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    @GetMapping("/summary")
    fun getMonthlySummary(
        @Parameter(
            description = "Year for which the transaction summary is requested.",
            example = "2026",
            required = true,
            schema = Schema(
                type = "integer",
                minimum = "1"
            )
        )
        @RequestParam
        @Min(value = 1, message = "Year must be greater than 0")
        year: Int,

        @Parameter(
            description = "Month for which the transaction summary is requested, from 1 (January) to 12 (December).",
            example = "10",
            required = true,
            schema = Schema(
                type = "integer",
                minimum = "1",
                maximum = "12"
            )
        )
        @RequestParam
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        month: Int
    ): ResponseEntity<MonthlySummaryResponse> {
        val summary = monthlySummaryService.getMonthlySummary(year, month)
        return ResponseEntity.ok(summary)
    }
}