package org.example.project.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.InetSocketAddress
import java.net.Socket

// ═══════════════════════════════════════════════
// ACTUAL CLASS — NetworkMonitor (Desktop/JVM)
// Desktop tidak punya ConnectivityManager,
// pakai socket ping ke DNS Google sebagai alternatif
// ═══════════════════════════════════════════════
actual class NetworkMonitor {

    actual fun isConnected(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    actual fun observeConnectivity(): Flow<Boolean> = flow {
        // Desktop: poll setiap 5 detik
        var lastStatus = isConnected()
        emit(lastStatus)
        while (true) {
            kotlinx.coroutines.delay(5_000)
            val current = isConnected()
            if (current != lastStatus) {
                lastStatus = current
                emit(current)
            }
        }
    }
}