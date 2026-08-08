package ru.netology.nmedia.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
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
    private var emptyColor = "#E0E0E0".toColorInt()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            emptyColor = getColor(R.styleable.StatsView_emptyColor, emptyColor)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        color = emptyColor
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Общий объём шкалы. Если не задан, сумма [data] считается за 100 %.
     * Значение меньше суммы данных автоматически расширяется до этой суммы.
     */
    var maxValue: Float? = null
        set(value) {
            field = value?.takeIf { it.isFinite() && it > 0F }
            invalidate()
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
        super.onDraw(canvas)

        val values = data.map { value ->
            value.takeIf { it.isFinite() && it > 0F } ?: 0F
        }
        val sum = values.sum()
        val total = maxOf(maxValue ?: sum, sum)

        if (total <= 0F) {
            return
        }

        canvas.drawCircle(center.x, center.y, radius, emptyPaint)

        var startFrom = -90F
        var firstSegmentColor: Int? = null
        for ((index, datum) in values.withIndex()) {
            val angle = 360F * datum / total
            if (angle <= 0F) {
                continue
            }
            val segmentColor = colors.getOrNull(index) ?: randomColor()
            firstSegmentColor = firstSegmentColor ?: segmentColor
            paint.color = segmentColor
            canvas.drawArc(oval, startFrom, angle, false, paint)
            startFrom += angle
        }

        firstSegmentColor?.let { color ->
            dotPaint.color = color
            canvas.drawCircle(center.x, oval.top, lineWidth / 2F, dotPaint)
        }

        canvas.drawText(
            "%.2f%%".format(sum / total * 100F),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}
