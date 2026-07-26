package com.wickwirez.myride.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object VinScanner {

    // VINs are 17 chars, excluding I, O, Q (ISO 3779) to avoid confusion with 1/0.
    private val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")

    suspend fun scanForVin(bitmap: Bitmap): String? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                var found: String? = null
                outer@ for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val condensed = line.text.uppercase().replace(Regex("[^A-Z0-9]"), "")
                        val match = vinRegex.find(condensed)
                        if (match != null) {
                            found = match.value
                            break@outer
                        }
                    }
                }
                continuation.resume(found)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
