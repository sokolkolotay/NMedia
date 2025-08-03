package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val actionValue = message.data[action]
        if (actionValue == null) {
            Log.w("Data messages", "Missing 'action' in push data: ${message.data}")
        }

        val actionEnum = try {
            actionValue?.let { Action.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Log.w("Data messages", "Unknown action: $actionValue")
        }

        when (actionEnum) {
            Action.LIKE -> handleLike(
                gson.fromJson(message.data[content], ActionLike::class.java)
            )

            Action.NEWPOST -> addedNewPost(
                gson.fromJson(message.data[content], ActionNewPost::class.java)
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.i("fcm_token", token)
    }

    private fun handleLike(like: ActionLike) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    like.userName,
                    like.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun addedNewPost(newPost: ActionNewPost) {
        val snippet = newPost.text.getSubjectAndSnippet()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle(
                getString(
                    R.string.notification_new_post,
                    newPost.userName)
            )
            .setContentText(snippet) // отображение короткого фрагмента поста
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(newPost.text) // отображение поста в развернутом виде
            )
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    fun String.getSubjectAndSnippet(maxLength: Int = 80): String {
        return if (this.length <= maxLength) this
        else this.substring(0, maxLength).trimEnd() + "..."
    }
}

enum class Action {
    LIKE,
    NEWPOST
}

data class ActionLike(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String
)

data class ActionNewPost(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String,
    val text: String
)