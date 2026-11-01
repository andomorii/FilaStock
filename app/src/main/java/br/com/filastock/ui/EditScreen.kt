@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.filastock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.filastock.MainViewModel
import br.com.filastock.data.COLOR_PRESETS
import br.com.filastock.data.MATERIALS
import br.com.filastock.data.Spool
import br.com.filastock.util.toDoubleBr

@Composable
fun EditScreen(
    vm: MainViewModel,
    id: Long,
    scannedCode: String?,
    onDone: () -> Unit,
) {
    val isNew = id == 0L
    val spoolFlow = remember(id) { vm.spool(id) }
    val existing by spoolFlow.collectAsStateWithLifecycle(initialValue = null)

    var material by rememberSaveable { mutableStateOf("PLA") }
    var brand by rememberSaveable { mutableStateOf("") }
    var colorName by rememberSaveable { mutableStateOf("Preto") }
    var colorHex by rememberSaveable { mutableStateOf(COLOR_PRESETS[0].argb) }
    var initial by rememberSaveable { mutableStateOf("1000") }
    var current by rememberSaveable { mutableStateOf("1000") }
    var price by rememberSaveable { mutableStateOf("") }
    var threshold by rememberSaveable { mutableStateOf("100") }
    var notes by rememberSaveable { mutableStateOf("") }
    var loaded by rememberSaveable { mutableStateOf(isNew) }

    LaunchedEffect(existing) {
        val e = existing
        if (!loaded && e != null) {
            material = e.material
            brand = e.brand
            colorName = e.colorName
            colorHex = e.colorHex
            initial = formatInput(e.initialWeight)
            current = formatInput(e.currentWeight)
            price = if (e.price > 0) formatInput(e.price) else ""
            threshold = formatInput(e.lowStockThreshold)
            notes = e.notes
            loaded = true
        }
    }

    val initialValue = initial.toDoubleBr()
    val currentValue = current.toDoubleBr()
    val valid = material.isNotBlank() &&
        colorName.isNotBlank() &&
        initialValue != null && initialValue > 0 &&
        currentValue != null && currentValue >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Novo carretel" else "Editar carretel") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
            if (isNew && !scannedCode.isNullOrBlank()) {
                Text(
                    "Carretel novo ligado ao QR code $scannedCode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Material", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = material,
                onValueChange = { material = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MATERIALS) { m ->
                    FilterChip(selected = material == m, onClick = { material = m }, label = { Text(m) })
                }
            }

            Text("Cor", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(COLOR_PRESETS) { preset ->
                    val selected = colorHex == preset.argb
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(preset.argb))
                            .border(
                                if (selected) 3.dp else 1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape,
                            )
                            .clickable {
                                val nameIsPreset = COLOR_PRESETS.any { it.name == colorName }
                                colorHex = preset.argb
                                if (colorName.isBlank() || nameIsPreset) colorName = preset.name
                            }
                    )
                }
            }
            OutlinedTextField(
                value = colorName,
                onValueChange = { colorName = it },
                label = { Text("Nome da cor (ex.: Preto, Azul Marinho)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marca (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = initial,
                onValueChange = {
                    if (isNew && current == initial) current = it
                    initial = it
                },
                label = { Text("Peso do filamento novo (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Peso restante agora (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Preço pago pelo carretel (R$)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("Avisar quando restar menos de (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (opcional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                enabled = valid,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val base = existing
                    val spool = if (base != null) {
                        base.copy(
                            material = material.trim(),
                            colorName = colorName.trim(),
                            colorHex = colorHex,
                            brand = brand.trim(),
                            initialWeight = initialValue ?: base.initialWeight,
                            currentWeight = currentValue ?: base.currentWeight,
                            price = price.toDoubleBr() ?: 0.0,
                            lowStockThreshold = threshold.toDoubleBr() ?: 0.0,
                            notes = notes.trim(),
                        )
                    } else {
                        Spool(
                            code = scannedCode?.takeIf { it.isNotBlank() } ?: br.com.filastock.data.newSpoolCode(),
                            material = material.trim(),
                            colorName = colorName.trim(),
                            colorHex = colorHex,
                            brand = brand.trim(),
                            initialWeight = initialValue ?: 1000.0,
                            currentWeight = currentValue ?: 1000.0,
                            price = price.toDoubleBr() ?: 0.0,
                            lowStockThreshold = threshold.toDoubleBr() ?: 0.0,
                            notes = notes.trim(),
                        )
                    }
                    vm.save(spool)
                    onDone()
                },
            ) {
                Text("Salvar")
            }
        }
    }
}

private fun formatInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
