package br.com.filastock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

fun newSpoolCode(): String = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()

/** Um carretel de filamento. Pesos em gramas, cor em ARGB. */
@Entity(tableName = "spools")
data class Spool(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Conteúdo do QR code da etiqueta do carretel. */
    val code: String = newSpoolCode(),
    val material: String,
    val colorName: String,
    val colorHex: Long,
    val brand: String = "",
    val initialWeight: Double,
    val currentWeight: Double,
    val price: Double = 0.0,
    val lowStockThreshold: Double = 100.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isLow: Boolean get() = currentWeight <= lowStockThreshold
    val fraction: Float
        get() = if (initialWeight > 0) (currentWeight / initialWeight).toFloat().coerceIn(0f, 1f) else 0f
    val pricePerGram: Double get() = if (initialWeight > 0) price / initialWeight else 0.0
    val remainingValue: Double get() = pricePerGram * currentWeight
}

/** Histórico: gramas > 0 = consumo; gramas < 0 = filamento acrescentado por ajuste. */
@Entity(
    tableName = "usage_logs",
    foreignKeys = [
        ForeignKey(
            entity = Spool::class,
            parentColumns = ["id"],
            childColumns = ["spoolId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("spoolId")],
)
data class UsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spoolId: Long,
    val grams: Double,
    val description: String = "",
    val cost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
)
