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
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Singleton
class AppAuth @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
    private val firebaseMessaging: FirebaseMessaging,
    private val api: PostApiService
) {

    private val prefs =
        context.getSharedPreferences(
            "auth",
            Context.MODE_PRIVATE
        )


    private val _state = MutableStateFlow(
        AuthState(
            id = prefs.getLong(ID_KEY, 0),
            token = prefs.getString(TOKEN_KEY, null)
        )
    )


    val state: StateFlow<AuthState> =
        _state.asStateFlow()


    fun setAuth(
        id: Long,
        token: String
    ) {

        prefs.edit()
            .putLong(ID_KEY, id)
            .putString(TOKEN_KEY, token)
            .apply()

        _state.value =
            AuthState(
                id,
                token
            )

        sendPushToken()
    }


    fun clearAuth() {

        prefs.edit()
            .remove(ID_KEY)
            .remove(TOKEN_KEY)
            .apply()

        _state.value =
            AuthState()

        sendPushToken()
    }


    fun sendPushToken(
        token: String? = null
    ) {

        CoroutineScope(Dispatchers.Default)
            .launch {

                try {

                    val pushToken =
                        token
                            ?: firebaseMessaging
                                .token
                                .awaitToken()


                    api.sendPushToken(
                        PushToken(pushToken)
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
    }


    companion object {

        private const val ID_KEY = "id"
        private const val TOKEN_KEY = "token"
    }
}


private suspend fun com.google.android.gms.tasks.Task<String>.awaitToken():
        String =
    suspendCancellableCoroutine { cont ->

        addOnSuccessListener {
            cont.resume(it)
        }

        addOnFailureListener {
            cont.resumeWithException(it)
        }
    }