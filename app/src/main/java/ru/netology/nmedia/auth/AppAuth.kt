package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.PushToken
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        AuthState(
            id = prefs.getLong(ID_KEY, 0),
            token = prefs.getString(TOKEN_KEY, null),
        )
    )
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun setAuth(id: Long, token: String) {
        prefs.edit()
            .putLong(ID_KEY, id)
            .putString(TOKEN_KEY, token)
            .apply()
        _state.value = AuthState(id, token)
        sendPushToken()
    }

    fun clearAuth() {
        prefs.edit()
            .remove(ID_KEY)
            .remove(TOKEN_KEY)
            .apply()
        _state.value = AuthState()
        sendPushToken()
    }

    // Отправляет FCM-токен на сервер с учётом текущей авторизации.
    // Вызывается при login/logout и при получении нового токена от FCM.
    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = token ?: FirebaseMessaging.getInstance().token.awaitToken()
                PostsApi.service.sendPushToken(PushToken(pushToken))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ID_KEY = "id"
        private const val TOKEN_KEY = "token"

        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(instance)

        fun init(context: Context): AppAuth = instance ?: synchronized(this) {
            instance ?: AppAuth(context.applicationContext).also { instance = it }
        }
    }
}

private suspend fun com.google.android.gms.tasks.Task<String>.awaitToken(): String =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }