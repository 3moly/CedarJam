package com.moly3.cedarjam.core.domain.func

fun findNewNameOrDefault(defaultName: String, existsNames: List<String>): String {
    var newCollectionName = defaultName
    var index = 0
    if (existsNames.firstOrNull { d -> d == newCollectionName } != null) {
        while (true) {
            newCollectionName = "Untitled$index"
            if (existsNames.firstOrNull { d -> d == newCollectionName } == null) {

                break
            }
            index++
        }
    }
    return newCollectionName
}