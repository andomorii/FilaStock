@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.filastock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.filastock.MainViewModel
import br.com.filastock.data.PrinterConfig
import br.com.filastock.util.extractSerial
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    scannedSerial: String?,
    onSerialConsumed: () -> Unit,
    onScanSerial: () -> Unit,
    onBack: () -> Unit,
) {
    val saved = vm.printer.value
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var ip by rememberSaveable { mutableStateOf(saved.ip) }
    var serial by rememberSaveable { mutableStateOf(saved.serial) }
    var accessCode by rememberSaveable { mutableStateOf(saved.accessCode) }
    var showCode by rememberSaveable { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(scannedSerial) {
        if (!scannedSerial.isNullOrBlank()) {
            serial = extractSerial(scannedSerial)
            onSerialConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Impressora Bambu Lab", style = MaterialTheme.typography.titleMedium)
            Text(
                "Ative o modo LAN no menu de rede da impressora e anote o IP e o código de acesso que aparecem lá. O celular precisa estar no mesmo Wi-Fi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome (opcional, ex.: A1 da oficina)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                label = { Text("Endereço IP (ex.: 192.168.0.25)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = serial,
                onValueChange = { serial = it },
                label = { Text("Número de série") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = onScanSerial) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Text("  Escanear QR da etiqueta")
            }
            OutlinedTextField(
                value = accessCode,
                onValueChange = { accessCode = it },
                label = { Text("Código de acesso (modo LAN)") },
                singleLine = true,
                visualTransformation = if (showCode) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCode = !showCode }) {
                        Icon(
                            if (showCode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar ou ocultar o código",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        vm.savePrinter(
                            PrinterConfig(
                                name = name.trim(),
                                ip = ip.trim(),
                                serial = serial.trim(),
                                accessCode = accessCode.trim(),
                            )
                        )
                        status = "Dados da impressora salvos."
                    },
                ) { Text("Salvar") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !testing && ip.isNotBlank(),
                    onClick = {
                        testing = true
                        status = "Testando..."
                        scope.launch {
                            status = vm.testPrinter(ip.trim())
                            testing = false
                        }
                    },
                ) { Text("Testar conexão") }
            }

            status?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(
                    "O desconto automático de filamento a cada impressão ainda não está ativo. Por enquanto, esta tela guarda os dados da impressora e testa se ela responde na rede; a leitura das impressões e do AMS lite é a próxima etapa.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
