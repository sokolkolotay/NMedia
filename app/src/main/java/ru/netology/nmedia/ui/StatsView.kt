package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private var animationType = ANIMATION_TYPE_PARALLEL

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private val startAnimation = Runnable(::update)

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            animationType = getInt(
                R.styleable.StatsView_animationType,
                ANIMATION_TYPE_PARALLEL,
            )
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            scheduleAnimation()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (data.isNotEmpty()) {
            scheduleAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(startAnimation)
        valueAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        val rotation = if (animationType == ANIMATION_TYPE_BIDIRECTIONAL) {
            0F
        } else {
            FULL_ROTATION * progress
        }
        var startFrom = -90F + rotation
        var consumedProgress = 0F
        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            val segmentProgress = when (animationType) {
                ANIMATION_TYPE_SEQUENTIAL -> if (datum > 0F) {
                    ((progress - consumedProgress) / datum).coerceIn(0F, 1F)
                } else {
                    0F
                }
                else -> progress
            }
            val segmentStart = if (animationType == ANIMATION_TYPE_BIDIRECTIONAL) {
                startFrom + angle * (1F - segmentProgress) / 2F
            } else {
                startFrom
            }
            paint.color = colors.getOrNull(index) ?: randomColor()
            if (segmentProgress > 0F) {
                canvas.drawArc(oval, segmentStart, angle * segmentProgress, false, paint)
            }
            startFrom += angle
            consumedProgress += datum.coerceAtLeast(0F)
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = ANIMATION_DURATION
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private companion object {
        const val FULL_ROTATION = 360F
        const val ANIMATION_DURATION = 2_000L
        const val ANIMATION_TYPE_PARALLEL = 0
        const val ANIMATION_TYPE_SEQUENTIAL = 1
        const val ANIMATION_TYPE_BIDIRECTIONAL = 2
    }

    private fun scheduleAnimation() {
        removeCallbacks(startAnimation)
        post(startAnimation)
    }
}
