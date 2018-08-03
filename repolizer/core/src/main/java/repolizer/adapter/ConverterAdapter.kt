package repolizer.adapter

abstract class ConverterAdapter<T> {
    abstract fun convertStringToData(repositoryClass: Class<*>, data: String): T?
    abstract fun convertDataToString(repositoryClass: Class<*>, data: Any): String?
}