package br.com.filastock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.filastock.data.ChangeResult
import br.com.filastock.data.PrinterConfig
import br.com.filastock.data.Spool
import br.com.filastock.data.UsageLog
import br.com.filastock.util.BAMBU_MQTT_PORT
import br.com.filastock.util.PrinterTester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as FilaStockApp).repository
    private val settings = (app as FilaStockApp).settings

    val spools: StateFlow<List<Spool>> =
        repo.spools.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _printer = MutableStateFlow(settings.load())
    val printer: StateFlow<PrinterConfig> = _printer

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

    fun savePrinter(config: PrinterConfig) {
        settings.save(config)
        _printer.value = config
    }

    suspend fun testPrinter(ip: String): String =
        if (PrinterTester.check(ip).isSuccess) {
            "A impressora respondeu em $ip (porta $BAMBU_MQTT_PORT)."
        } else {
            "Sem resposta em $ip:$BAMBU_MQTT_PORT. Confira o IP, se o celular está no mesmo Wi-Fi e se o modo LAN está ativo."
        }

    /** Avisa só quando o carretel cruza o limite, para não repetir a notificação a cada uso. */
    private fun notifyIfCrossedThreshold(result: ChangeResult?) {
        if (result == null) return
        if (!result.before.isLow && result.after.isLow) {
            Notifier.lowStock(getApplication<Application>(), result.after)
        }
    }
}
