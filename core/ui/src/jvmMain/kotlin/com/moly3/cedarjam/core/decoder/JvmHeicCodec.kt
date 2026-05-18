package com.moly3.cedarjam.core.decoder
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Strategy for turning HEIC bytes into PNG bytes on the JVM.
 *
 * The JVM has no native HEIC support: neither `javax.imageio.ImageIO` nor Skia
 * (which Compose Desktop renders with) can decode HEVC-coded images. We
 * therefore delegate to an external codec.
 *
 * Three concrete strategies are provided, tried in this order by
 * [JvmHeicCodec.detect]:
 *
 *  1. [LibHeifConvertCodec]  — `heif-convert` from libheif. Lightest, best.
 *  2. [ImageMagickCodec]     — `magick` / `convert`. Common, single distributable.
 *  3. [SipsCodec]            — macOS built-in `sips`. Zero bundling on macOS.
 */
interface JvmHeicCodec {

    /** Human-readable name for logging / diagnostics. */
    val name: String

    /** Decode a HEIC file at [input] and write PNG output to [output]. */
    fun convertToPng(input: File, output: File)

    companion object {
        /**
         * Detects the first usable codec.
         *
         * [explicitBinary], if non-null, forces use of a specific binary path
         * (e.g. one you bundle and ship with your Compose Desktop app).
         */
        fun detect(explicitBinary: String? = null): JvmHeicCodec? {
            if (explicitBinary != null) {
                val f = File(explicitBinary)
                if (f.canExecute()) return LibHeifConvertCodec(explicitBinary)
            }
            // Probe PATH, then well-known install locations.
            return sequenceOf(
                LibHeifConvertCodec.locate(),
                ImageMagickCodec.locate(),
                SipsCodec.locate(),
            ).firstOrNull { it != null }
        }
    }
}

/** Runs [command], enforces a timeout, and fails loudly with captured output. */
internal fun runProcess(command: List<String>, timeoutSeconds: Long = 30) {
    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()

    // Drain the stream so the child can't block on a full pipe buffer.
    val output = process.inputStream.bufferedReader().readText()

    val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
    if (!finished) {
        process.destroyForcibly()
        error("HEIC codec timed out after ${timeoutSeconds}s: ${command.first()}")
    }
    val exit = process.exitValue()
    check(exit == 0) {
        "HEIC codec '${command.first()}' failed (exit $exit):\n${output.take(2000)}"
    }
}

/** Returns the first [candidates] entry that exists and is executable. */
internal fun firstExecutable(candidates: List<String>): String? =
    candidates.firstOrNull { path ->
        runCatching { File(path).canExecute() }.getOrDefault(false) ||
                runCatching { onPath(path) }.getOrDefault(false)
    }

/** True if [name] resolves on the system PATH. */
private fun onPath(name: String): Boolean {
    val pathDirs = System.getenv("PATH")?.split(File.pathSeparatorChar).orEmpty()
    val exeNames = if (isWindows()) listOf("$name.exe", "$name.cmd", name) else listOf(name)
    return pathDirs.any { dir ->
        exeNames.any { exe -> File(dir, exe).canExecute() }
    }
}

internal fun isWindows(): Boolean =
    System.getProperty("os.name").lowercase().contains("win")

internal fun isMac(): Boolean =
    System.getProperty("os.name").lowercase().let { it.contains("mac") || it.contains("darwin") }

// ---------------------------------------------------------------------------
// libheif's heif-convert
// ---------------------------------------------------------------------------

internal class LibHeifConvertCodec(private val binary: String) : JvmHeicCodec {
    override val name: String get() = "libheif:heif-convert ($binary)"

    override fun convertToPng(input: File, output: File) {
        // heif-convert infers the output format from the file extension.
        runProcess(listOf(binary, input.absolutePath, output.absolutePath))
    }

    companion object {
        fun locate(): LibHeifConvertCodec? {
            val candidates = buildList {
                add("heif-convert")
                add("/usr/bin/heif-convert")
                add("/usr/local/bin/heif-convert")
                add("/opt/homebrew/bin/heif-convert")     // Apple-silicon Homebrew
                if (isWindows()) add("C:\\Program Files\\libheif\\heif-convert.exe")
            }
            return firstExecutable(candidates)?.let(::LibHeifConvertCodec)
        }
    }
}

// ---------------------------------------------------------------------------
// ImageMagick
// ---------------------------------------------------------------------------

internal class ImageMagickCodec(private val binary: String) : JvmHeicCodec {
    override val name: String get() = "ImageMagick ($binary)"

    override fun convertToPng(input: File, output: File) {
        // IM7 uses `magick in out`; IM6 uses `convert in out`.
        val args = if (binary.endsWith("magick") || binary.endsWith("magick.exe")) {
            listOf(binary, input.absolutePath, output.absolutePath)
        } else {
            listOf(binary, input.absolutePath, output.absolutePath)
        }
        runProcess(args)
    }

    companion object {
        fun locate(): ImageMagickCodec? {
            val candidates = buildList {
                add("magick")
                add("convert")
                add("/usr/bin/magick")
                add("/usr/local/bin/magick")
                add("/opt/homebrew/bin/magick")
            }
            return firstExecutable(candidates)?.let(::ImageMagickCodec)
        }
    }
}

// ---------------------------------------------------------------------------
// macOS sips (built into every macOS install)
// ---------------------------------------------------------------------------

internal class SipsCodec(private val binary: String) : JvmHeicCodec {
    override val name: String get() = "macOS sips"

    override fun convertToPng(input: File, output: File) {
        runProcess(
            listOf(
                binary, "-s", "format", "png",
                input.absolutePath, "--out", output.absolutePath,
            ),
        )
    }

    companion object {
        fun locate(): SipsCodec? {
            if (!isMac()) return null
            return firstExecutable(listOf("/usr/bin/sips", "sips"))?.let(::SipsCodec)
        }
    }
}