package com.example.tickets_2.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object NetworkUtils {
    fun fetchDataFromServer(url: String, onResponse: (Response) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            onResponse(response)
        }
    }
}
