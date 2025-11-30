actual fun getTestResourcePath(resourceName: String): String {
    val classLoader = object {}.javaClass.classLoader
    return classLoader.getResource(resourceName)?.file ?: ""
//    val inputStream = classLoader.getResourceAsStream(resourceName)
//    return inputStream?.bufferedReader()?.use { it.readText() }
//        ?: throw IllegalArgumentException("Resource not found: $resourceName")
}