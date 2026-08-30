package com.wickwirez.myride.data

/**
 * VIN cleanup and validation.
 *
 * Standard VINs never contain I, O, or Q (excluded to avoid confusion with
 * 1 and 0), so any of those in a scanned VIN is an OCR misread we can correct
 * automatically. The check digit then catches most remaining single-character
 * errors before we waste a network call on an invalid VIN.
 */
object VinValidator {

    private val TRANSLITERATION = mapOf(
        'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6, 'G' to 7, 'H' to 8,
        'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4, 'N' to 5, 'P' to 7, 'R' to 9,
        'S' to 2, 'T' to 3, 'U' to 4, 'V' to 5, 'W' to 6, 'X' to 7, 'Y' to 8, 'Z' to 9,
        '0' to 0, '1' to 1, '2' to 2, '3' to 3, '4' to 4,
        '5' to 5, '6' to 6, '7' to 7, '8' to 8, '9' to 9
    )

    private val WEIGHTS = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)

    /** Uppercases, strips non-alphanumerics, and fixes characters a VIN can never contain. */
    fun clean(raw: String): String =
        raw.uppercase()
            .filter { it.isLetterOrDigit() }
            .map { c ->
                when (c) {
                    'I' -> '1'
                    'O', 'Q' -> '0'
                    else -> c
                }
            }
            .joinToString("")

    /** True if the VIN is 17 characters and its check digit (position 9) is valid. */
    fun isValid(vin: String): Boolean {
        if (vin.length != 17) return false
        if (vin.any { TRANSLITERATION[it] == null }) return false

        var sum = 0
        vin.forEachIndexed { index, c ->
            val value = TRANSLITERATION[c] ?: return false
            sum += value * WEIGHTS[index]
        }

        val remainder = sum % 11
        val expected = if (remainder == 10) 'X' else ('0' + remainder)
        return vin[8] == expected
    }

    /**
     * Returns a user-facing problem description, or null if the VIN looks fine.
     */
    fun problemWith(vin: String): String? = when {
        vin.isBlank() -> "Enter a VIN first"
        vin.length != 17 -> "A VIN should be 17 characters — this one is ${vin.length}"
        !isValid(vin) -> "That VIN doesn't look right — double-check it for misread characters"
        else -> null
    }
}
