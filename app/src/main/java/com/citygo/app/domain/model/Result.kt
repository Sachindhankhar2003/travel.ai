package com.citygo.app.domain.model

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: retrofit2.HttpException) {
        Result.Error("Server error: ${e.code()} ${e.message()}", e)
    } catch (e: java.net.UnknownHostException) {
        Result.Error("No internet connection", e)
    } catch (e: java.net.SocketTimeoutException) {
        Result.Error("Request timed out", e)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error", e)
    }
}
