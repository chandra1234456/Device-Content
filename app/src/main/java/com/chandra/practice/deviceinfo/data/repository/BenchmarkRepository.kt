package com.chandra.practice.deviceinfo.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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

    suspend fun runBenchmark(onProgress: (Float, String) -> Unit): BenchmarkResult = withContext(Dispatchers.Default) {
        // Step 1: CPU Benchmark (Prime calculations & multi-threaded iteration)
        onProgress(0.1f, "Testing CPU Performance...")
        val cpuStartTime = System.currentTimeMillis()
        var primeCount = 0
        val maxNumber = 250_000
        for (i in 2..maxNumber) {
            var isPrime = true
            val limit = sqrt(i.toDouble()).toInt()
            for (j in 2..limit) {
                if (i % j == 0) {
                    isPrime = false
                    break
                }
            }
            if (isPrime) primeCount++
        }
        val cpuTimeMs = (System.currentTimeMillis() - cpuStartTime).coerceAtLeast(1)
        // Score calculation: Faster time -> higher score (normalized against 300ms target)
        val cpuScore = ((3000.0 / cpuTimeMs) * 1000).toInt().coerceIn(100, 10000)

        // Step 2: RAM Memory Benchmark (Buffer creation & block copying)
        onProgress(0.4f, "Testing RAM Speed...")
        val ramStartTime = System.currentTimeMillis()
        val bufferSize = 10 * 1024 * 1024 // 10MB
        val srcArray = ByteArray(bufferSize) { (it % 256).toByte() }
        val dstArray = ByteArray(bufferSize)
        val iterations = 5
        for (k in 0 until iterations) {
            System.arraycopy(srcArray, 0, dstArray, 0, bufferSize)
        }
        val ramTimeMs = (System.currentTimeMillis() - ramStartTime).coerceAtLeast(1)
        val totalRamMbCopied = (bufferSize.toDouble() * iterations) / (1024 * 1024)
        val ramSpeedMbps = (totalRamMbCopied / (ramTimeMs / 1000.0))
        val ramScore = (ramSpeedMbps * 2.5).toInt().coerceIn(100, 10000)

        // Step 3: Storage Benchmark (File write & read throughput)
        onProgress(0.7f, "Testing Storage Speed...")
        val testFile = File(context.cacheDir, "benchmark_test.tmp")
        val storageDataSize = 8 * 1024 * 1024 // 8MB
        var writeSpeedMbps = 0.0
        var readSpeedMbps = 0.0

        try {
            val testBuffer = ByteArray(storageDataSize) { (it % 256).toByte() }

            val writeStartTime = System.currentTimeMillis()
            testFile.writeBytes(testBuffer)
            val writeTimeMs = (System.currentTimeMillis() - writeStartTime).coerceAtLeast(1)
            writeSpeedMbps = ((storageDataSize.toDouble() / (1024 * 1024)) / (writeTimeMs / 1000.0))

            val readStartTime = System.currentTimeMillis()
            testFile.readBytes()
            val readTimeMs = (System.currentTimeMillis() - readStartTime).coerceAtLeast(1)
            readSpeedMbps = ((storageDataSize.toDouble() / (1024 * 1024)) / (readTimeMs / 1000.0))
        } finally {
            if (testFile.exists()) {
                testFile.delete()
            }
            System.gc()
        }

        val storageScore = ((writeSpeedMbps * 1.5) + (readSpeedMbps * 2.0)).toInt().coerceIn(100, 10000)

        // Overall Score & Rating Tier
        onProgress(1.0f, "Finalizing Results...")
        val overallScore = ((cpuScore * 0.45) + (ramScore * 0.35) + (storageScore * 0.20)).toInt()

        val tierRating = when {
            overallScore >= 6500 -> "Flagship Grade"
            overallScore >= 4000 -> "High Performance"
            overallScore >= 2200 -> "Mid-Range Grade"
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
}
