package ru.netology.nmedia.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    }

    fun clearAuth() {
        prefs.edit()
            .remove(ID_KEY)
            .remove(TOKEN_KEY)
            .apply()
        _state.value = AuthState()
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