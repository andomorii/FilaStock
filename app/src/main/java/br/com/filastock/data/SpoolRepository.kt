package br.com.filastock.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

data class ChangeResult(val before: Spool, val after: Spool)

class SpoolRepository(private val db: AppDatabase) {
    private val dao = db.spoolDao()

    val spools: Flow<List<Spool>> = dao.observeAll()

    fun spool(id: Long): Flow<Spool?> = dao.observe(id)

    fun logs(spoolId: Long): Flow<List<UsageLog>> = dao.observeLogs(spoolId)

    suspend fun findByCode(code: String): Spool? = dao.getByCode(code)

    suspend fun save(spool: Spool): Long =
        if (spool.id == 0L) {
            dao.insert(spool)
        } else {
            dao.update(spool)
            spool.id
        }

    suspend fun delete(spool: Spool) = dao.delete(spool)

    /** Desconta [grams] do carretel e registra no histórico, tudo em uma transação. */
    suspend fun consume(spoolId: Long, grams: Double, description: String): ChangeResult? =
        db.withTransaction {
            val before = dao.get(spoolId) ?: return@withTransaction null
            val after = before.copy(currentWeight = (before.currentWeight - grams).coerceAtLeast(0.0))
            dao.update(after)
            dao.insertLog(
                UsageLog(
                    spoolId = spoolId,
                    grams = grams,
                    description = description,
                    cost = grams * before.pricePerGram,
                )
            )
            ChangeResult(before, after)
        }

    /** Corrige o peso restante (por exemplo, depois de pesar o carretel na balança). */
    suspend fun setWeight(spoolId: Long, newWeight: Double): ChangeResult? =
        db.withTransaction {
            val before = dao.get(spoolId) ?: return@withTransaction null
            val after = before.copy(currentWeight = newWeight.coerceAtLeast(0.0))
            dao.update(after)
            dao.insertLog(
                UsageLog(
                    spoolId = spoolId,
                    grams = before.currentWeight - after.currentWeight,
                    description = "Ajuste por pesagem",
                    cost = 0.0,
                )
            )
            ChangeResult(before, after)
        }
}
