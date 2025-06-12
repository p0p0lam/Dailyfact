package com.popolam.app.dailyfact.services

import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

class FactFirebaseMessagingService: FirebaseMessagingService(), KoinComponent {

    val job  = SupervisorJob() + Dispatchers.Main
    val coroutineScope = CoroutineScope(job)
    override fun onMessageReceived(message: com.google.firebase.messaging.RemoteMessage) {
        Timber.d("Message received: ${message.data}")
        coroutineScope.launch {

        }
    }
    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}