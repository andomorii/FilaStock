package br.com.filastock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpoolDao {
    @Query("SELECT * FROM spools ORDER BY material, colorName")
    fun observeAll(): Flow<List<Spool>>

    @Query("SELECT * FROM spools WHERE id = :id")
    fun observe(id: Long): Flow<Spool?>

    @Query("SELECT * FROM spools WHERE id = :id")
    suspend fun get(id: Long): Spool?

    @Query("SELECT * FROM spools WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): Spool?

    @Insert
    suspend fun insert(spool: Spool): Long

    @Update
    suspend fun update(spool: Spool)

    @Delete
    suspend fun delete(spool: Spool)

    @Query("SELECT * FROM usage_logs WHERE spoolId = :spoolId ORDER BY timestamp DESC")
    fun observeLogs(spoolId: Long): Flow<List<UsageLog>>

    @Insert
    suspend fun insertLog(log: UsageLog)
}
