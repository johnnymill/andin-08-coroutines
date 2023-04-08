package ru.netology.coroutines

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.coroutines.dto.Comment
import ru.netology.coroutines.dto.Post
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

private val BASE_URL = "http://127.0.0.1:9999"
private val gson = Gson()
private val client = OkHttpClient.Builder()
    .connectTimeout(10_000, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()

private suspend fun OkHttpClient.apiCall(url: String): Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(this::newCall)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

            })
    }
}

private suspend fun <T> makeRequest(client: OkHttpClient, url: String, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        gson.fromJson(client.apiCall(url).body?.string(), typeToken)
    }

private suspend fun getPosts(client: OkHttpClient): List<Post> =
    makeRequest(
        client = client,
        url = "$BASE_URL/api/slow/posts",
        typeToken = object : TypeToken<List<Post>>() {}
    )

private suspend fun getComments(client: OkHttpClient, id: Long): List<Comment> =
    makeRequest(
        client = client,
        url = "$BASE_URL/api/slow/posts/$id/comments",
        typeToken = object : TypeToken<List<Comment>>() {}
    )

fun main() {
//    testCoroutines()
    testCoroutinesPosts()

    Thread.sleep(60_000L)
}

private fun testCoroutines() {
    runBlocking {
        println(Thread.currentThread().name)
    }
    val myDispatcher = Executors.newFixedThreadPool(64).asCoroutineDispatcher()
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch(Dispatchers.Default) {
            println(Thread.currentThread().name)
        }
//        launch(Dispatchers.Main) {
//            println(Thread.currentThread().name)
//        }
        launch(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
        launch(Dispatchers.Unconfined) {
            println(Thread.currentThread().name)
        }
        launch(myDispatcher) {
            println(Thread.currentThread().name)
        }
    }
    Thread.sleep(1_000L)
    myDispatcher.close()
}

private fun testCoroutinesPosts() {
    CoroutineScope(EmptyCoroutineContext).launch {
        val durationSyncMs = measureTimeMillis {
            val posts = getPosts(client)
            posts.forEach { post ->
                getComments(client, post.id)
            }
        }
        println("Duration sync = $durationSyncMs ms")
    }


    CoroutineScope(EmptyCoroutineContext).launch {
        val durationAsyncMs = measureTimeMillis {
            val posts = getPosts(client)
            posts.map { post ->
                async {
                    getComments(client, post.id)
                }
            }.awaitAll()
        }
        println("Duration async = $durationAsyncMs ms")
    }
}
