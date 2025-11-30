package com.moly3.cedarjam.core.storage.json.canvas

import com.moly3.cedarjam.core.domain.model.ResultWrapper

//fun main() {
//    val jsonInput = """
//    {"shapes":[{"id":1,"position":{"x":-293.0,"y":-610.0},"size":{"x":200.0,"y":200.0},"fileData":{"type":"Text","text":"123"}},{"id":2,"position":{"x":80.43361,"y":-277.10385},"size":{"x":200.0,"y":200.0},"fileData":{"type":"Text","text":"123"}},{"id":3,"position":{"x":-294.12726,"y":-186.84755},"size":{"x":200.0,"y":350.0},"color":"#ffff4900","fileData":{"type":"core.domain.model.FileDataJson.FileNode","relativeToFilePath":"moly3/resources/png/Screenshot 2022-01-01 at 06.34.42.png"}},{"id":4,"position":{"x":421.51517,"y":-215.25887},"size":{"x":325.0,"y":350.0},"fileData":{"type":"core.domain.model.FileDataJson.FileNode","relativeToFilePath":"moly3/resources/jpg/pvpb9i6bd7951.jpg"}}],"connections":[{"id":1760983713138,"fromBox":2,"toBox":1,"fromSide":"LEFT","toSide":"RIGHT","color":null},{"id":1761049136723,"fromBox":3,"toBox":2,"fromSide":"RIGHT","toSide":"LEFT","color":null},{"id":1761996949299,"fromBox":2,"toBox":4,"fromSide":"BOTTOM","toSide":"LEFT","color":null}]}
//    """.trimIndent()
//
//    val parser = CanvasDataParser()
//    val data = parser.parse(jsonInput)
//
//    // Вывод результатов
//    println("=== SHAPES ===")
//    data.shapes.forEachIndexed { index, result ->
//        when (result) {
//            is ResultWrapper.Success -> {
//                println("Shape #$index: OK - ID=${result.value.id}")
//            }
//            is ResultWrapper.Error -> {
//                println("Shape #$index: ERROR - ${result.error}")
//                println("  Raw JSON: ${result.error.rawJson}")
//            }
//        }
//    }
//
//    println("\n=== CONNECTIONS ===")
//    data.connections.forEachIndexed { index, result ->
//        when (result) {
//            is ResultWrapper.Success -> {
//                println("Connection #$index: OK - ID=${result.value.id}")
//            }
//            is ResultWrapper.Error -> {
//                println("Connection #$index: ERROR - ${result.error}")
//                println("  Raw JSON: ${result.error.rawJson}")
//            }
//        }
//    }
//
//    // Сохранение обратно
//    println("\n=== SERIALIZED ===")
//    val serialized = parser.serialize(data)
//    println(serialized)
//
//    // Статистика
//    val successfulShapes = data.shapes.count { it is ResultWrapper.Success }
//    val errorShapes = data.shapes.count { it is ResultWrapper.Error }
//    val successfulConnections = data.connections.count { it is ResultWrapper.Success }
//    val errorConnections = data.connections.count { it is ResultWrapper.Error }
//
//    println("\n=== STATISTICS ===")
//    println("Shapes: $successfulShapes OK, $errorShapes errors")
//    println("Connections: $successfulConnections OK, $errorConnections errors")
//}