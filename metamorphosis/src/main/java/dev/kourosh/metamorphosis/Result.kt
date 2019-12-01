package dev.kourosh.metamorphosis

sealed class Result<out T : Any> {
    class Success<out T : Any>(val data: T) : Result<T>()
    class Error(val message: String, val code: Int?) : Result<Nothing>()
}