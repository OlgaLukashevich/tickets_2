package com.example.tickets_2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class startView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        CoroutineScope(Dispatchers.Main).launch {
        // Эмулируем загрузку данных, например, с помощью задержки
        simulateLoading()

        // После загрузки данных переходим к MainActivity
        val intent = Intent(this@startView, MainActivity::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активити, чтобы пользователь не мог вернуться к экрану загрузки
        }
    }
    private suspend fun simulateLoading() {
        delay(1000) // Эмулируем задержку в 2 секунды
    }
}
