package org.example.project.data

import kotlinx.coroutines.flow.Flow

expect class NetworkMonitor() {
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}