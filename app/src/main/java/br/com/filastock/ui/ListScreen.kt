@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.filastock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.filastock.MainViewModel
import br.com.filastock.data.Spool
import br.com.filastock.util.grams
import br.com.filastock.util.money

@Composable
fun ListScreen(
    vm: MainViewModel,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit,
    onScan: () -> Unit,
    onSettings: () -> Unit,
) {
    val spools by vm.spools.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val materials = spools.map { it.material }.distinct().sorted()
    val shown = spools.filter { s ->
        (filter == null || s.material == filter) &&
            (query.isBlank() || "${s.material} ${s.colorName} ${s.brand}".contains(query, ignoreCase = true))
    }
    val lowStock = spools.filter { it.isLow }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FilaStock") },
                actions = {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR code")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Novo carretel")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SummaryCard(spools) }

            if (lowStock.isNotEmpty()) {
                item { LowStockBanner(lowStock) }
            }

            if (spools.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por cor, marca ou material") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (materials.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filter == null,
                                onClick = { filter = null },
                                label = { Text("Todos") },
                            )
                        }
                        items(materials) { m ->
                            FilterChip(
                                selected = filter == m,
                                onClick = { filter = if (filter == m) null else m },
                                label = { Text(m) },
                            )
                        }
                    }
                }
            }

            if (spools.isEmpty()) {
                item { EmptyState() }
            }

            items(shown, key = { it.id }) { spool ->
                SpoolCard(spool, onClick = { onOpen(spool.id) })
            }
        }
    }
}

@Composable
private fun SummaryCard(spools: List<Spool>) {
    val totalGrams = spools.sumOf { it.currentWeight }
    val totalValue = spools.sumOf { it.remainingValue }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryItem("Carretéis", spools.size.toString())
            SummaryItem("Em estoque", String.format("%.2f kg", totalGrams / 1000.0))
            SummaryItem("Valor", totalValue.money())
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LowStockBanner(low: List<Spool>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Estoque baixo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    low.joinToString { "${it.material} ${it.colorName} (${it.currentWeight.grams()})" },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nenhum carretel cadastrado", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Toque no + para adicionar o primeiro.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SpoolCard(spool: Spool, onClick: () -> Unit) {
    val low = spool.isLow
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SpoolThumb(spool, 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("${spool.material} · ${spool.colorName}", style = MaterialTheme.typography.titleMedium)
                val subtitle = (listOf(spool.brand) + spool.finishList)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { spool.fraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    spool.currentWeight.grams(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (low) {
                    Text(
                        "Estoque baixo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
