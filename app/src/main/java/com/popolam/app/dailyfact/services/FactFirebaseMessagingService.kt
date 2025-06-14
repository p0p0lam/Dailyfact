package com.popolam.app.dailyfact.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.popolam.app.dailyfact.data.repository.PushRepository
import com.popolam.app.dailyfact.data.repository.PushRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class FactFirebaseMessagingService: FirebaseMessagingService(), KoinComponent {

    private val job  = SupervisorJob() + Dispatchers.Main
    private val coroutineScope = CoroutineScope(job)
    private val pushRepository: PushRepository by inject()

    override fun onMessageReceived(message: com.google.firebase.messaging.RemoteMessage) {
        Timber.d("Message received: ${message.data}")
        message.data.get("secret_key")?.let {
            coroutineScope.launch { pushRepository.saveSecret(it) }
        }
    }
    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        coroutineScope.launch { pushRepository.processPushToken(token)}
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}