package com.wickwirez.myride.data

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object VinScanner {

    // VINs are 17 chars, excluding I, O, Q (ISO 3779) to avoid confusion with 1/0.
    private val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")

    fun extractVin(text: String): String? {
        val condensed = text.uppercase().replace(Regex("[^A-Z0-9]"), "")
        return vinRegex.find(condensed)?.value
    }

    // Kept for any one-shot Bitmap use; live scanning uses scanImageForVin below.
    suspend fun scanForVin(bitmap: Bitmap): String? =
        scanImageForVin(InputImage.fromBitmap(bitmap, 0))

    // Tries the printed VIN text first, then falls back to the barcode —
    // works for both a static image and a single live camera frame.
    suspend fun scanImageForVin(image: InputImage): String? {
        return scanTextForVin(image) ?: scanBarcodeForVin(image)
    }

    private suspend fun scanTextForVin(image: InputImage): String? = suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                var found: String? = null
                outer@ for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val match = extractVin(line.text)
                        if (match != null) {
                            found = match
                            break@outer
                        }
                    }
                }
                if (continuation.isActive) continuation.resume(found)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }

    private suspend fun scanBarcodeForVin(image: InputImage): String? = suspendCancellableCoroutine { continuation ->
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                var found: String? = null
                for (barcode in barcodes) {
                    val raw = barcode.rawValue ?: continue
                    val match = extractVin(raw)
                    if (match != null) {
                        found = match
                        break
                    }
                }
                if (continuation.isActive) continuation.resume(found)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }
}
