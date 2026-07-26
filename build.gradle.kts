plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
    // NOTE: verify this KSP version still matches Kotlin 2.3.20 at
    // https://github.com/google/ksp/releases before building — KSP
    // versions are pinned to a specific Kotlin release and this repo
    // was scaffolded from memory of your stated toolchain, not a live check.
    id("com.google.devtools.ksp") version "2.3.9" apply false
}
