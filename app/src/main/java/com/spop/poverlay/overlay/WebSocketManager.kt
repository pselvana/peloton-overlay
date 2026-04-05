package com.spop.poverlay.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    fun connect(url: String) {
        disconnect()
        if (url.isBlank()) return
        try {
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Timber.i("WebSocket connected to $url")
                    _isConnected.value = true
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.i("WebSocket closing: $reason")
                    webSocket.close(1000, null)
                    _isConnected.value = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.i("WebSocket closed")
                    _isConnected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Timber.e(t, "WebSocket error")
                    _isConnected.value = false
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Failed to create WebSocket connection to $url")
            _isConnected.value = false
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Disconnecting")
        } catch (e: Exception) {
            // Ignore errors during close
        }
        webSocket = null
        _isConnected.value = false
    }

    fun send(json: String) {
        webSocket?.send(json)
    }
}
