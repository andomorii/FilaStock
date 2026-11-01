@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.filastock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.filastock.MainViewModel
import br.com.filastock.data.Spool
import br.com.filastock.data.UsageLog
import br.com.filastock.util.QR_PREFIX
import br.com.filastock.util.dateTime
import br.com.filastock.util.grams
import br.com.filastock.util.money
import br.com.filastock.util.qrBitmap
import br.com.filastock.util.toDoubleBr
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class DetailDialog { USE, WEIGH, QR, DELETE }

@Composable
fun DetailScreen(
    vm: MainViewModel,
    id: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val spoolFlow = remember(id) { vm.spool(id) }
    val logsFlow = remember(id) { vm.logs(id) }
    val spool by spoolFlow.collectAsStateWithLifecycle(initialValue = null)
    val logs by logsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var dialog by remember { mutableStateOf<DetailDialog?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spool?.let { "${it.material} · ${it.colorName}" } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { dialog = DetailDialog.DELETE }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                },
            )
        },
    ) { padding ->
        val s = spool
        if (s != null) {
            Column(
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(s.colorHex, 56.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(s.colorName, style = MaterialTheme.typography.headlineSmall)
                        val subtitle = if (s.brand.isBlank()) s.material else "${s.material} · ${s.brand}"
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Text(
                    s.currentWeight.grams(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (s.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
                LinearProgressIndicator(
                    progress = { s.fraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (s.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
                Text(
                    "${(s.fraction * 100).roundToInt()}% de ${s.initialWeight.grams()}",
                    style = MaterialTheme.typography.bodySmall,
                )

                if (s.isLow) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Estoque baixo: abaixo de ${s.lowStockThreshold.grams()}.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoRow("Valor restante", s.remainingValue.money())
                        InfoRow("Preço pago", s.price.money())
                        InfoRow("Preço por kg", (s.pricePerGram * 1000).money())
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { dialog = DetailDialog.USE }) { Text("Registrar uso") }
                    OutlinedButton(onClick = { dialog = DetailDialog.WEIGH }) { Text("Pesar") }
                    OutlinedButton(onClick = { dialog = DetailDialog.QR }) { Text("QR") }
                }

                if (s.notes.isNotBlank()) {
                    Text(s.notes, style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider()
                Text("Histórico", style = MaterialTheme.typography.titleMedium)
                if (logs.isEmpty()) {
                    Text(
                        "Nenhum uso registrado ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                logs.forEach { LogRow(it) }
            }

            when (dialog) {
                DetailDialog.USE -> UseDialog(
                    spool = s,
                    onDismiss = { dialog = null },
                    onConfirm = { grams, description ->
                        vm.consume(s.id, grams, description)
                        dialog = null
                    },
                )
                DetailDialog.WEIGH -> WeighDialog(
                    onDismiss = { dialog = null },
                    onConfirm = { newWeight ->
                        vm.setWeight(s.id, newWeight)
                        dialog = null
                    },
                )
                DetailDialog.QR -> QrDialog(s, onDismiss = { dialog = null })
                DetailDialog.DELETE -> AlertDialog(
                    onDismissRequest = { dialog = null },
                    title = { Text("Excluir carretel?") },
                    text = { Text("O carretel e o histórico dele serão apagados.") },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.delete(s)
                            dialog = null
                            onBack()
                        }) { Text("Excluir") }
                    },
                    dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancelar") } },
                )
                null -> Unit
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LogRow(log: UsageLog) {
    val amount = if (log.grams >= 0) "−${log.grams.grams(1)}" else "+${abs(log.grams).grams(1)}"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(
                log.description.ifBlank { "Uso" },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                log.timestamp.dateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (log.cost > 0) {
                Text(
                    log.cost.money(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UseDialog(spool: Spool, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var gramsText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val grams = gramsText.toDoubleBr()
    val valid = grams != null && grams > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar uso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { gramsText = it },
                    label = { Text("Gramas usadas") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Peça impressa (opcional)") },
                    singleLine = true,
                )
                if (grams != null && grams > 0) {
                    Text("Custo: ${(grams * spool.pricePerGram).money()}")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { if (grams != null) onConfirm(grams, description.trim()) },
            ) { Text("Descontar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun WeighDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var scaleText by remember { mutableStateOf("") }
    var tareText by remember { mutableStateOf("250") }
    val scale = scaleText.toDoubleBr()
    val tare = tareText.toDoubleBr()
    val result = if (scale != null && tare != null) scale - tare else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pesar carretel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = scaleText,
                    onValueChange = { scaleText = it },
                    label = { Text("Peso na balança (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = tareText,
                    onValueChange = { tareText = it },
                    label = { Text("Peso do carretel vazio (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                if (result != null) {
                    Text("Filamento restante: ${result.coerceAtLeast(0.0).grams(0)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = result != null && result >= 0,
                onClick = { if (result != null) onConfirm(result) },
            ) { Text("Atualizar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun QrDialog(spool: Spool, onDismiss: () -> Unit) {
    val bitmap = remember(spool.code) { qrBitmap(QR_PREFIX + spool.code) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR code do carretel") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR code ${spool.code}",
                    modifier = Modifier.size(240.dp),
                )
                Text(spool.code, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tire print, imprima e cole no carretel. Ao escanear, o app abre este carretel.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}
