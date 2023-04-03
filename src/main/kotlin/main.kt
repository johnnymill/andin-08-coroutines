package ru.netology.coroutines

import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.EmptyCoroutineContext

fun main() {
    runBlocking {
        println(Thread.currentThread().name)
    }
    val myDispatcher = Executors.newFixedThreadPool(64).asCoroutineDispatcher()
    with (CoroutineScope(EmptyCoroutineContext)) {
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
