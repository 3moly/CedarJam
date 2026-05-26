package shared_tests.base

actual fun getTestResourcePath(resourceName: String): String {
    val classLoader = object {}.javaClass.classLoader
    return classLoader.getResource(resourceName)?.file ?: ""
}