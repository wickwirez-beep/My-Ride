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

    // ISO 3779 / NHTSA check-digit transliteration + position weights (position 9 is the check digit).
    private val transliteration = mapOf(
        'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6, 'G' to 7, 'H' to 8,
        'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4, 'N' to 5, 'P' to 7, 'R' to 9,
        'S' to 2, 'T' to 3, 'U' to 4, 'V' to 5, 'W' to 6, 'X' to 7, 'Y' to 8, 'Z' to 9
    )
    private val weights = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)

    private fun expectedCheckDigit(vin: String): Char? {
        if (vin.length != 17) return null
        var sum = 0
        for (i in vin.indices) {
            if (i == 8) continue
            val c = vin[i]
            val value = if (c.isDigit()) c - '0' else transliteration[c] ?: return null
            sum += value * weights[i]
        }
        val remainder = sum % 11
        return if (remainder == 10) 'X' else ('0' + remainder)
    }

    /** True if the VIN's own embedded check digit (position 9) matches the computed one. */
    fun isValidChecksum(vin: String): Boolean {
        val expected = expectedCheckDigit(vin) ?: return false
        return vin.length == 17 && vin[8] == expected
    }

    fun extractVin(text: String): String? {
        val condensed = text.uppercase().replace(Regex("[^A-Z0-9]"), "")
        if (condensed.length < 17) return null

        // Slide a 17-char window across every position instead of just taking the first
        // match — a stray leading/trailing character (logo glyph, sticker border text)
        // otherwise shifts the whole window and corrupts the VIN.
        val candidates = mutableListOf<String>()
        for (start in 0..(condensed.length - 17)) {
            val window = condensed.substring(start, start + 17)
            if (vinRegex.matches(window)) candidates.add(window)
        }
        if (candidates.isEmpty()) return null

        // Prefer whichever candidate's own check digit actually validates.
        return candidates.firstOrNull { isValidChecksum(it) } ?: candidates.first()
    }

    data class VinResult(val vin: String, val checksumValid: Boolean)

    /** Same as extractVin, but also reports whether the result passed checksum —
     * use this if you want to warn the user instead of silently trusting a bad scan. */
    fun extractVinWithValidation(text: String): VinResult? {
        val vin = extractVin(text) ?: return null
        return VinResult(vin, isValidChecksum(vin))
    }

    suspend fun scanForVin(bitmap: Bitmap): String? =
        scanImageForVin(InputImage.fromBitmap(bitmap, 0))

    suspend fun scanImageForVin(image: InputImage): String? {
        return scanTextForVin(image) ?: scanBarcodeForVin(image)
    }

    private suspend fun scanTextForVin(image: InputImage): String? = suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val candidates = mutableListOf<String>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        extractVin(line.text)?.let { candidates.add(it) }
                    }
                }
                // Prefer a checksum-valid candidate across ALL lines, not just the first line matched.
                val result = candidates.firstOrNull { isValidChecksum(it) } ?: candidates.firstOrNull()
                if (continuation.isActive) continuation.resume(result)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }

    private suspend fun scanBarcodeForVin(image: InputImage): String? = suspendCancellableCoroutine { continuation ->
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val candidates = mutableListOf<String>()
                for (barcode in barcodes) {
                    val raw = barcode.rawValue ?: continue
                    extractVin(raw)?.let { candidates.add(it) }
                }
                val result = candidates.firstOrNull { isValidChecksum(it) } ?: candidates.firstOrNull()
                if (continuation.isActive) continuation.resume(result)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }
}
