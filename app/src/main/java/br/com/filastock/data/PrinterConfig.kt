package br.com.filastock.data

import android.content.Context

data class PrinterConfig(
    val name: String = "",
    val ip: String = "",
    val serial: String = "",
    val accessCode: String = "",
)

/** Guarda os dados da impressora no armazenamento privado do app. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("printer", Context.MODE_PRIVATE)

    fun load(): PrinterConfig = PrinterConfig(
        name = prefs.getString("name", "") ?: "",
        ip = prefs.getString("ip", "") ?: "",
        serial = prefs.getString("serial", "") ?: "",
        accessCode = prefs.getString("accessCode", "") ?: "",
    )

    fun save(config: PrinterConfig) {
        prefs.edit()
            .putString("name", config.name)
            .putString("ip", config.ip)
            .putString("serial", config.serial)
            .putString("accessCode", config.accessCode)
            .apply()
    }
}
