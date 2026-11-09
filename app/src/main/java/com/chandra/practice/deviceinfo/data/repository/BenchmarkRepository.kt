package com.chandra.practice.deviceinfo.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

data class BenchmarkResult(
    val cpuScore: Int,
    val ramScore: Int,
    val storageScore: Int,
    val overallScore: Int,
    val tierRating: String,
    val details: BenchmarkDetails,
)

data class BenchmarkDetails(
    val primeTimeMs: Long,
    val memoryThroughputMbps: Double,
    val storageWriteMbps: Double,
    val storageReadMbps: Double,
)

class BenchmarkRepository(private val context: Context) {

    /**
     * Scores are relative to fixed reference workloads, not calibrated against other devices, so
     * treat them as a way to compare phones (and runs) within this app.
     *
     * Everything is timed with [System.nanoTime]: the previous millisecond clock rounded these short
     * tests to 1-4 ms and hit the score cap on any fast phone.
     */
    suspend fun runBenchmark(onProgress: (Float, String) -> Unit): BenchmarkResult = withContext(Dispatchers.Default) {
        // Step 1: CPU. Count primes up to a fixed limit, split across every core.
        onProgress(0.1f, "Testing CPU performance...")
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        countPrimesParallel(CPU_WARMUP_LIMIT, cores) // warm up the JIT so the timed run is representative
        val cpuStart = System.nanoTime()
        val primeCount = countPrimesParallel(CPU_LIMIT, cores)
        val cpuTimeMs = ((System.nanoTime() - cpuStart) / 1_000_000L).coerceAtLeast(1)
        check(primeCount > 0)
        val cpuScore = (CPU_REFERENCE_MS * 2000.0 / cpuTimeMs).toInt().coerceIn(100, 10000)

        // Step 2: RAM. Repeated large block copies (after one warm-up pass).
        onProgress(0.4f, "Testing RAM speed...")
        val bufferSize = 8 * 1024 * 1024
        val src = ByteArray(bufferSize) { (it % 256).toByte() }
        val dst = ByteArray(bufferSize)
        System.arraycopy(src, 0, dst, 0, bufferSize)
        val iterations = 40
        val ramStart = System.nanoTime()
        repeat(iterations) { System.arraycopy(src, 0, dst, 0, bufferSize) }
        val ramSeconds = ((System.nanoTime() - ramStart) / 1e9).coerceAtLeast(1e-6)
        val ramSpeedMbps = (bufferSize.toDouble() * iterations / (1024 * 1024)) / ramSeconds
        val ramScore = (ramSpeedMbps * 0.5).toInt().coerceIn(100, 10000)

        // Step 3: Storage. The write is synced to disk so it measures the storage, not the OS cache.
        // The read usually comes from the cache (it can't be bypassed without root), so it counts less.
        onProgress(0.7f, "Testing storage speed...")
        val testFile = File(context.cacheDir, "benchmark_test.tmp")
        val storageBytes = 16 * 1024 * 1024
        var writeSpeedMbps = 0.0
        var readSpeedMbps = 0.0
        try {
            val buffer = ByteArray(storageBytes) { (it % 256).toByte() }
            val writeStart = System.nanoTime()
            FileOutputStream(testFile).use { out ->
                out.write(buffer)
                out.fd.sync()
            }
            val writeSeconds = ((System.nanoTime() - writeStart) / 1e9).coerceAtLeast(1e-6)
            writeSpeedMbps = (storageBytes.toDouble() / (1024 * 1024)) / writeSeconds

            val readStart = System.nanoTime()
            testFile.readBytes()
            val readSeconds = ((System.nanoTime() - readStart) / 1e9).coerceAtLeast(1e-6)
            readSpeedMbps = (storageBytes.toDouble() / (1024 * 1024)) / readSeconds
        } finally {
            if (testFile.exists()) testFile.delete()
        }
        val storageScore = (writeSpeedMbps * 5.0 + readSpeedMbps * 0.5).toInt().coerceIn(100, 10000)

        onProgress(1.0f, "Finalizing results...")
        val overallScore = ((cpuScore * 0.45) + (ramScore * 0.35) + (storageScore * 0.20)).toInt()
        val tierRating = when {
            overallScore >= 5500 -> "Flagship Grade"
            overallScore >= 3500 -> "High Performance"
            overallScore >= 1800 -> "Mid-Range Grade"
            else -> "Entry Level"
        }

        BenchmarkResult(
            cpuScore = cpuScore,
            ramScore = ramScore,
            storageScore = storageScore,
            overallScore = overallScore,
            tierRating = tierRating,
            details = BenchmarkDetails(
                primeTimeMs = cpuTimeMs,
                memoryThroughputMbps = ramSpeedMbps,
                storageWriteMbps = writeSpeedMbps,
                storageReadMbps = readSpeedMbps,
            ),
        )
    }

    /** Counts primes in 2..[limit], splitting the range into chunks spread over [threads] workers. */
    private suspend fun countPrimesParallel(limit: Int, threads: Int): Int = coroutineScope {
        val chunks = threads * 8
        val size = (limit / chunks).coerceAtLeast(1)
        (0 until chunks).map { c ->
            async {
                val from = maxOf(2, c * size)
                val to = if (c == chunks - 1) limit else (c + 1) * size - 1
                var count = 0
                for (n in from..to) {
                    var isPrime = true
                    val root = sqrt(n.toDouble()).toInt()
                    var d = 2
                    while (d <= root) {
                        if (n % d == 0) { isPrime = false; break }
                        d++
                    }
                    if (isPrime) count++
                }
                count
            }
        }.awaitAll().sum()
    }

    private companion object {
        const val CPU_WARMUP_LIMIT = 200_000
        const val CPU_LIMIT = 3_000_000

        /** Time at which the CPU test scores 2000; faster scores higher, up to the 10000 cap. */
        const val CPU_REFERENCE_MS = 1000L
    }
}
