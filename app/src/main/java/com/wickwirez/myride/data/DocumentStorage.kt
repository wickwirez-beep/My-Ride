package com.wickwirez.myride.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object DocumentStorage {

    suspend fun copyToInternalStorage(
        context: Context,
        sourceUri: Uri,
        mimeType: String?
    ): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
                val extension = mimeType?.let {
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
                } ?: "dat"
                val destFile = File(docsDir, "${UUID.randomUUID()}.$extension")

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    Pair(Uri.fromFile(destFile).toString(), destFile.name)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
