package org.example.budget_tracker_api.category

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.example.budget_tracker_api.database.Category
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {

    @Operation(
        summary = "Get all categories",
        description = "Returns all categories sorted by name in ascending order."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Categories retrieved successfully",
        content = [
            Content(mediaType = "application/json", schema = Schema(type = "array", implementation = CategoryResponse::class))
        ]
    )
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = categoryRepository.findAllByOrderByNameAsc()

        val response = categories.map { category ->
            CategoryResponse(
                id = requireNotNull(category.id),
                name = category.name,
                color = category.color
            )
        }

        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Create a new category",
        description = "Creates a category with a unique name (case-insensitive) and a hex color format."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Category created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CategoryResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input", content = [Content()]),
            ApiResponse(responseCode = "409", description = "Category name already exists", content = [Content()])
        ]
    )
    @PostMapping
    fun createCategory(
        @Valid @RequestBody
           @io.swagger.v3.oas.annotations.parameters.RequestBody(
               description = "Category data required for creation",
               required = true,
               content = [Content(mediaType = "application/json", schema = Schema(implementation = CategoryRequest::class))]
           )
           request: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val name = request.name.trim()

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists.")
        }

        val category = Category(name = name, color = request.color)

        val savedCategory = categoryRepository.save(category)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CategoryResponse(
                id = requireNotNull(savedCategory.id),
                name = savedCategory.name,
                color = savedCategory.color
            )
        )
    }

    @Operation(
        summary = "Update an existing category",
        description = "Modifies the name or color of an existing category by its ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Category updated successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Category::class))]),
            ApiResponse(responseCode = "400", description = "Invalid input", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Category not found", content = [Content()]),
            ApiResponse(responseCode = "409", description = "Category name already exists", content = [Content()])
        ]
    )
    @PutMapping("/{id}")
    fun updateCategory(
        @Parameter(
            description = "Unique ID of the category to update",
            example = "1",
            required = true,
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable id: Long,

        @Valid @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated category name and color",
            required = true,
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = CategoryRequest::class))
            ]
        )
        updatedData: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        val newName = updatedData.name.trim()

        val existingWithSameName = categoryRepository.findByNameIgnoreCase(newName)

        if (existingWithSameName.isPresent && existingWithSameName.get().id != id
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists.")
        }

        category.name = newName
        category.color = updatedData.color

        val savedCategory = categoryRepository.save(category)

        return ResponseEntity.ok(
            CategoryResponse(
                id = requireNotNull(savedCategory.id),
                name = savedCategory.name,
                color = savedCategory.color
            )
        )
    }

    @Operation(
        summary = "Delete a category",
        description = "Deletes a category by its ID. Fails if the category has associated transactions."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            ApiResponse(responseCode = "409", description = "Cannot delete category because it has transactions", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Category not found", content = [Content()])
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteCategory(
        @Parameter(
            description = "Unique ID of the category to delete",
            example = "1",
            required = true,
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }

        if (transactionRepository.existsByCategoryId(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A category that has transactions cannot be deleted.")
        }

        categoryRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }


    private fun Category.toResponse(): CategoryResponse =
        CategoryResponse(
            id = requireNotNull(id) { "Category ID must not be null" },
            name = name,
            color = color
        )

    private fun CategoryRequest.toEntity(): Category =
        Category(
            name = name.trim(),
            color = color
        )
}