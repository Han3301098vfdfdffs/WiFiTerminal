// TerminalViewModel.kt
package com.example.wifiterminal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TerminalViewModel : ViewModel() {
    private val _connectionStatus = MutableStateFlow(TcpClient.ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<TcpClient.ConnectionStatus> = _connectionStatus

    private val _messages = MutableStateFlow(listOf("Esperando datos..."))
    val messages: StateFlow<List<String>> = _messages

    private lateinit var tcpClient: TcpClient

    fun initializeClient(ip: String, port: Int) {
        tcpClient = TcpClient(
            ip = ip,
            port = port,
            onMessageReceived = { message ->
                viewModelScope.launch {
                    _messages.value = _messages.value + message
                }
            },
            onConnectionStatusChanged = { status ->
                viewModelScope.launch {
                    _connectionStatus.value = status
                    if (status == TcpClient.ConnectionStatus.CONNECTED) {
                        _messages.value = _messages.value + "Conexión establecida"
                    }
                }
            }
        )
    }

    fun toggleConnection() {
        when (connectionStatus.value) {
            TcpClient.ConnectionStatus.CONNECTED -> tcpClient.stopClient()
            else -> tcpClient.connect()
        }
    }

    fun clearTerminal() {
        _messages.value = listOf("Terminal limpiada - ${getCurrentDateTime()}")
    }

    fun saveToSharedPreferences(context: Context) {
        val prefs = context.getSharedPreferences("TerminalPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("terminal_content", messages.value.joinToString("\n"))
        editor.apply()
    }

    fun saveToTextFile(context: Context) {
        val fileName = "terminal_${getCurrentDateTimeFile()}.txt"
        val content = messages.value.joinToString("\n")

        try {
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveToCSV(context: Context) {
        val fileName = "terminal_${getCurrentDateTimeFile()}.csv"
        val content = messages.value.joinToString("\n") { "\"$it\"" }

        try {
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDateTimeFile(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    // Agregar al TerminalViewModel
    fun saveToDatabase(context: Context) {
        viewModelScope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.terminalDao().insert(
                    TerminalEntry(content = messages.value.joinToString("\n"))
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}