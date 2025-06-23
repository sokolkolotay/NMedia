package ru.netology.nmedia.util

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver.OnWindowFocusChangeListener
import android.view.inputmethod.InputMethodManager
import kotlin.math.floor

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // thanks to https://stackoverflow.com/a/68925063/1219012
    fun showKeyboard(view: View) {
        view.requestFocus()
        if (view.hasWindowFocus()) {
            showKeyboardNow(view)
        } else {
            view.viewTreeObserver.addOnWindowFocusChangeListener(object : OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        showKeyboardNow(view)
                        view.viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
        }
    }

    private fun showKeyboardNow(view: View) {
        if (!view.isFocused) return

        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> {
            val rounding = floor(count / 100.0) / 10
            "${rounding}K".replace(".0", "")
        }

        count < 1_000_000 -> "${count / 1_000}K"
        else -> {
            val rounding = floor(count / 100_000.0) / 10
            "${rounding}M".replace(".0", "")
        }
    }
}