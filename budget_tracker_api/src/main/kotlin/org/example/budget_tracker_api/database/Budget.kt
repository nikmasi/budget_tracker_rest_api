package org.example.budget_tracker_api.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal

@Entity
@Table(
    name = "budget",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_budget_month_category",
            columnNames = ["month", "category_id"]
        )
    ]
)
class Budget(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Month is required")
    @field:Pattern(regexp = "^\\d{4}-(?:0[1-9]|1[0-2])$", message = "Month must be in format YYYY-MM")
    @Column(nullable = false, length = 7)
    var month: String, // Format: YYYY-MM

    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @field:DecimalMax(value = "1000000.00", message = "Amount must be at most 1,000,000.00")
    @field:Digits(integer = 7, fraction = 2, message = "Amount must have at most 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category
)