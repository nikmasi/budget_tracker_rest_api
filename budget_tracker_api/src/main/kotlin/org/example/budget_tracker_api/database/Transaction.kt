package org.example.budget_tracker_api.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.budget_tracker_api.transaction.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "transaction")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 10)
    var type: TransactionType,

    @field:DecimalMin("0.01")
    @field:DecimalMax("1000000.00")
    @field:Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @field:NotNull
    @field:Column(nullable = false)
    var date: LocalDate,

    @field:Size(max = 200)
    @field:Column(length = 200)
    var description: String? = null,

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(name = "category_id", nullable = false)
    var category: Category
)