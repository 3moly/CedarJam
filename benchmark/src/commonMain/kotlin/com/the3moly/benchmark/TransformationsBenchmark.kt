package com.the3moly.benchmark

import kotlinx.benchmark.*
import kotlin.random.Random
//
////@State(Scope.Benchmark)
////@Warmup(iterations = 3, time = 1)
////@Measurement(iterations = 3, time = 1)
//private class TransformationsBenchmark {
//    @Param( "111", "1111")
//    var length: Int = 0
//
//    var array: IntArray = IntArray(0)
//
//    @Setup
//    fun allocateArray() {
//        val rnd = Random(length)
//        array = IntArray(length) { rnd.nextInt() }
//    }
//
//    @Benchmark
//    fun sortedTakeLast() = array.sorted().takeLast(5)
//
////    @Benchmark
////    fun sortedArrayTakeLast() = array.sortedArray().takeLast(5)
////
////    @Benchmark
////    fun sortedDescendingTake() = array.sortedDescending().take(5)
//
//    @Benchmark
//    fun sortedArrayDescendingTake() = array.sortedArrayDescending().take(5)
//}