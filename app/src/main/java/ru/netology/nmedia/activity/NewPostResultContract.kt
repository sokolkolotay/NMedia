package ru.netology.nmedia.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class NewPostResultContract : ActivityResultContract<String?, String?>() {

    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, NewPostActivity::class.java).apply {
            input?.let {
                putExtra(Intent.EXTRA_TEXT, it)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return intent?.getStringExtra(Intent.EXTRA_TEXT)
    }
}
