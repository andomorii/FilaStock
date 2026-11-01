package br.com.filastock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.filastock.data.Spool
import br.com.filastock.util.PhotoStore
import coil.compose.AsyncImage

@Composable
fun ColorDot(argb: Long, size: Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(argb))
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

/** Primeira foto do carretel, ou a bolinha da cor quando não há foto. */
@Composable
fun SpoolThumb(spool: Spool, size: Dp) {
    val context = LocalContext.current
    val first = spool.photoList.firstOrNull()
    if (first != null) {
        AsyncImage(
            model = PhotoStore.file(context, first),
            contentDescription = "Foto do filamento",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
    } else {
        ColorDot(spool.colorHex, size)
    }
}
