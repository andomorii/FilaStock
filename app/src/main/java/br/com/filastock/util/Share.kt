package br.com.filastock.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.com.filastock.data.Spool
import java.io.File

/**
 * Texto para enviar a clientes: só características.
 * Preço, peso, estoque e observações internas nunca entram aqui.
 */
fun Spool.publicDescription(): String = buildString {
    append("$material · $colorName")
    if (brand.isNotBlank()) append("\nMarca: $brand")
    if (finishList.isNotEmpty()) append("\nAcabamento: ${finishList.joinToString(", ")}")
}

/** Abre a lista de apps (WhatsApp, etc.) com uma foto e o texto. */
fun shareToChat(context: Context, text: String, photo: File?) {
    val send = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        if (photo != null && photo.exists()) {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", photo)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Foto", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }
    val chooser = Intent.createChooser(send, "Compartilhar filamento")
    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(chooser)
}
