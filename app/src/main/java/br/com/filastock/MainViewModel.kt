package br.com.filastock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.filastock.data.ChangeResult
import br.com.filastock.data.Spool
import br.com.filastock.data.UsageLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as FilaStockApp).repository

    val spools: StateFlow<List<Spool>> =
        repo.spools.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun spool(id: Long): Flow<Spool?> = repo.spool(id)

    fun logs(id: Long): Flow<List<UsageLog>> = repo.logs(id)

    suspend fun findByCode(code: String): Spool? = repo.findByCode(code)

    fun save(spool: Spool) {
        viewModelScope.launch { repo.save(spool) }
    }

    fun delete(spool: Spool) {
        viewModelScope.launch { repo.delete(spool) }
    }

    fun consume(spoolId: Long, grams: Double, description: String) {
        viewModelScope.launch { notifyIfCrossedThreshold(repo.consume(spoolId, grams, description)) }
    }

    fun setWeight(spoolId: Long, newWeight: Double) {
        viewModelScope.launch { notifyIfCrossedThreshold(repo.setWeight(spoolId, newWeight)) }
    }

    /** Avisa só quando o carretel cruza o limite, para não repetir a notificação a cada uso. */
    private fun notifyIfCrossedThreshold(result: ChangeResult?) {
        if (result == null) return
        if (!result.before.isLow && result.after.isLow) {
            Notifier.lowStock(getApplication<Application>(), result.after)
        }
    }
}
