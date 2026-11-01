@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.filastock.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.filastock.MainViewModel
import br.com.filastock.data.COLOR_PRESETS
import br.com.filastock.data.FINISHES
import br.com.filastock.data.MATERIALS
import br.com.filastock.data.Spool
import br.com.filastock.data.newSpoolCode
import br.com.filastock.util.PhotoStore
import br.com.filastock.util.toDoubleBr
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun EditScreen(
    vm: MainViewModel,
    id: Long,
    scannedCode: String?,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
    var finishesCsv by rememberSaveable { mutableStateOf("") }
    var photosCsv by rememberSaveable { mutableStateOf("") }
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
            finishesCsv = e.finishes
            photosCsv = e.photos
            loaded = true
        }
    }

    val photoNames = photosCsv.split(",").filter { it.isNotBlank() }
    val selectedFinishes = finishesCsv.split(",").filter { it.isNotBlank() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val added = uris.mapNotNull { PhotoStore.import(context, it) }
                val now = photosCsv.split(",").filter { it.isNotBlank() }
                photosCsv = (now + added).joinToString(",")
            }
        }
    }

    // Foto tirada pela câmera: a câmera grava num arquivo temporário, que depois é
    // importado (reduzido e girado certo) e apagado.
    var capturePath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = capturePath
        capturePath = null
        if (path != null) {
            val temp = File(path)
            if (ok) {
                scope.launch {
                    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", temp)
                    val name = PhotoStore.import(context, uri)
                    temp.delete()
                    if (name != null) {
                        val now = photosCsv.split(",").filter { it.isNotBlank() }
                        photosCsv = (now + name).joinToString(",")
                    }
                }
            } else {
                temp.delete()
            }
        }
    }
    fun startCapture() {
        val dir = File(context.cacheDir, "capture").apply { mkdirs() }
        val file = File(dir, UUID.randomUUID().toString() + ".jpg")
        capturePath = file.absolutePath
        val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        takePicture.launch(uri)
    }
    // O app declara a permissão de câmera (para ler QR code), então ela precisa estar
    // concedida antes de abrir a câmera do celular.
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCapture()
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

            Text("Acabamento", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FINISHES.forEach { finish ->
                    val on = finish in selectedFinishes
                    FilterChip(
                        selected = on,
                        onClick = {
                            val next = if (on) selectedFinishes - finish else selectedFinishes + finish
                            finishesCsv = next.joinToString(",")
                        },
                        label = { Text(finish) },
                    )
                }
            }

            Text("Fotos", style = MaterialTheme.typography.titleSmall)
            Text(
                "Fotografe com boa luz natural: ajuda a ver a cor real.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (photoNames.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoNames) { name ->
                        Box {
                            AsyncImage(
                                model = PhotoStore.file(context, name),
                                contentDescription = "Foto do filamento",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                            IconButton(
                                onClick = { photosCsv = photoNames.filter { it != name }.joinToString(",") },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color(0x99000000), CircleShape),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remover foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) startCapture() else cameraPermission.launch(Manifest.permission.CAMERA)
                    },
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("  Tirar foto")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Text("  Galeria")
                }
            }

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
                label = { Text("Observações internas (não são compartilhadas)") },
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
                            finishes = finishesCsv,
                            photos = photosCsv,
                        )
                    } else {
                        Spool(
                            code = scannedCode?.takeIf { it.isNotBlank() } ?: newSpoolCode(),
                            material = material.trim(),
                            colorName = colorName.trim(),
                            colorHex = colorHex,
                            brand = brand.trim(),
                            initialWeight = initialValue ?: 1000.0,
                            currentWeight = currentValue ?: 1000.0,
                            price = price.toDoubleBr() ?: 0.0,
                            lowStockThreshold = threshold.toDoubleBr() ?: 0.0,
                            notes = notes.trim(),
                            finishes = finishesCsv,
                            photos = photosCsv,
                        )
                    }
                    // Apaga do aparelho as fotos que foram removidas deste carretel.
                    existing?.photoList
                        ?.filter { it !in photoNames }
                        ?.forEach { PhotoStore.delete(context, it) }
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
