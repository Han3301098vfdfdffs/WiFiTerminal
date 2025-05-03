// TerminalScreen.kt
package com.example.wifiterminal

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }

    // Inicialización del cliente
    LaunchedEffect(Unit) {
        viewModel.initializeClient(ip = "192.168.1.72", port = 12345)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConnectionStatusIndicator(connectionStatus)
        Spacer(modifier = Modifier.height(16.dp))
        ConnectionButton(
            status = connectionStatus,
            onToggleConnection = { viewModel.toggleConnection() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Nuevos botones de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.clearTerminal() }) {
                Text("Limpiar Terminal")
            }

            Button(onClick = { showSaveDialog = true }) {
                Text("Guardar Terminal")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TerminalDisplay(messages = messages, scrollState = scrollState)
    }

    // Auto-scroll
    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Diálogo para seleccionar método de guardado
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar terminal") },
            text = { Text("Seleccione el método de almacenamiento:") },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            viewModel.saveToSharedPreferences(context)
                            showSaveDialog = false
                        }
                    ) {
                        Text("SharedPreferences")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveToTextFile(context)
                            showSaveDialog = false
                        }
                    ) {
                        Text("Archivo de texto")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveToCSV(context)
                            showSaveDialog = false
                        }
                    ) {
                        Text("Archivo CSV")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveToDatabase(context)
                            showSaveDialog = false
                        }
                    ) {
                        Text("Base de datos (Room)")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {showSaveDialog = false},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cancelar")
                    }
                }
            }
        )
    }
}

@Composable
private fun ConnectionStatusIndicator(status: TcpClient.ConnectionStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Estado:")
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    color = when (status) {
                        TcpClient.ConnectionStatus.DISCONNECTED -> Color.Gray
                        TcpClient.ConnectionStatus.CONNECTING -> Color.Yellow
                        TcpClient.ConnectionStatus.CONNECTED -> Color.Green
                        TcpClient.ConnectionStatus.ERROR -> Color.Red
                    },
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun ConnectionButton(
    status: TcpClient.ConnectionStatus,
    onToggleConnection: () -> Unit
) {
    Button(onClick = onToggleConnection) {
        Text(
            text = when (status) {
                TcpClient.ConnectionStatus.CONNECTED -> "Desconectar"
                else -> "Conectar"
            }
        )
    }
}

@Composable
private fun TerminalDisplay(messages: List<String>, scrollState: ScrollState) {
    Text("Terminal:", style = MaterialTheme.typography.titleMedium)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {
            messages.forEach { message ->
                Text(message, modifier = Modifier.padding(4.dp))
            }
        }
    }
}