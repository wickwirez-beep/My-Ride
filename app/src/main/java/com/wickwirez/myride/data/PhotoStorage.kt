package com.wickwirez.myride.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object PhotoStorage {

    // Copies the picked photo into the app's own internal storage and returns
    // a stable file:// URI to it. Storing the raw picked URI directly is not
    // reliable long-term — that grant can be lost (e.g. after a reboot),
    // leaving the image unable to load. A local copy always works.
    suspend fun copyToInternalStorage(context: Context, sourceUri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
                val destFile = File(photosDir, "${UUID.randomUUID()}.jpg")

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    Uri.fromFile(destFile).toString()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
