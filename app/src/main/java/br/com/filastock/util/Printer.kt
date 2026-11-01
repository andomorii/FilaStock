package br.com.filastock.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/** Porta MQTT que a impressora Bambu Lab abre na rede local. */
const val BAMBU_MQTT_PORT = 8883

object PrinterTester {
    /** Só verifica se a porta da impressora responde na rede. Não faz login. */
    suspend fun check(ip: String, timeoutMs: Int = 3000): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                Socket().use { it.connect(InetSocketAddress(ip, BAMBU_MQTT_PORT), timeoutMs) }
            }
        }
}

/** Tenta achar o número de série (15 letras/números) no texto lido do QR code. */
fun extractSerial(raw: String): String =
    Regex("[0-9A-Z]{15}").find(raw.uppercase())?.value ?: raw.trim()
