package com.the3moly.benchmark

import com.moly3.cedarjam.core.data.FilesRepository
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import kotlinx.benchmark.*
import kotlin.random.Random

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
open class GetNodesBenchmark {
//    @Param("111", "1111")
//    var length: Int = 0

    lateinit var filesRepo: FilesRepository

    @Setup
    fun allocateArray() {
        filesRepo = FilesRepository(createSystemFilesManager())
    }

    @Benchmark
    fun getNodes() {
        filesRepo.getNodes(
            workspacePath = "/Users/new07/Desktop/generated",
            "/Users/new07/Desktop/generated"
        )
    }
}