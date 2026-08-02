package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {


    @Inject
    lateinit var appAuth: AppAuth


    private val contentKey = "content"
    private val channelId = "remote"

    private val gson = Gson()



    override fun onCreate() {

        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name =
                getString(R.string.channel_remote_name)

            val descriptionText =
                getString(R.string.channel_remote_description)


            val channel =
                NotificationChannel(
                    channelId,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {

                    description = descriptionText
                }


            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager


            manager.createNotificationChannel(channel)
        }
    }



    override fun onMessageReceived(
        message: RemoteMessage
    ) {

        val raw =
            message.data[contentKey]
                ?: return


        val pushMessage =
            try {

                gson.fromJson(
                    raw,
                    PushMessage::class.java
                )

            } catch (e: Exception) {

                return
            }



        val myId =
            appAuth.state.value.id



        when {

            pushMessage.recipientId == null ->
                showSimpleNotification(
                    pushMessage.content
                )


            pushMessage.recipientId == myId ->
                showSimpleNotification(
                    pushMessage.content
                )


            else ->
                appAuth.sendPushToken()
        }
    }



    override fun onNewToken(
        token: String
    ) {

        appAuth.sendPushToken(token)
    }



    private fun showSimpleNotification(
        text: String
    ) {

        val notification =
            NotificationCompat.Builder(
                this,
                channelId
            )
                .setSmallIcon(
                    R.drawable.ic_notification
                )
                .setContentText(text)
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .build()


        notify(notification)
    }



    private fun notify(
        notification: Notification
    ) {

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            NotificationManagerCompat
                .from(this)
                .notify(
                    Random.nextInt(100000),
                    notification
                )
        }
    }
}



data class PushMessage(
    val recipientId: Long?,
    val content: String,
)