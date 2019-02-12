package com.hm60.circularchart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt

class CircularChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr : Int = 0) : View(context, attrs, defStyleAttr) {

    var ringSize: Float = toPx(10)
        set(value) {
            field = value
            invalidate()
        }

    var isCurve:Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var startAngle = 0
        set(value) {
            field = value
            invalidate()
        }

    var clockwise:Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var outerCircleRadius = toPx(60)

    val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var innerCircleRadius = toPx(40)

    val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val outerOval = RectF()
    val innerOval = RectF()

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    val textBound = Rect()


    var progress = 0F
        set(value) {
            field = value
            invalidate()
        }

    var path = Path()

    fun setProgressColor(@ColorInt color:Int){
        arcPaint.color = color
        textPaint.color = color
        invalidate()
    }

    fun setRingBackgroundColor(@ColorInt color:Int){
        outerCirclePaint.color = color
        invalidate()
    }

    fun setCircleRadius(radius:Float){
        outerCircleRadius = radius
        invalidate()
    }

    init {
        outerCirclePaint.color = Color.parseColor("#F0F0F0")
        outerCirclePaint.style = Paint.Style.FILL
        innerCirclePaint.color = Color.parseColor("#FFFFFF")
        innerCirclePaint.style = Paint.Style.FILL

        arcPaint.style = Paint.Style.FILL

        if (attrs != null) {
            val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.CircularChart, defStyleAttr, 0)

            arcPaint.color = attributes.getColor(R.styleable.CircularChart_progressColor, Color.GREEN)
            outerCirclePaint.color =
                attributes.getColor(R.styleable.CircularChart_ringBackgroundColor, Color.parseColor("#F0F0F0"))
            progress = attributes.getInt(R.styleable.CircularChart_progress, 0).toFloat()

            outerCircleRadius =
                attributes.getDimensionPixelSize(R.styleable.CircularChart_circleRadius, toPx(60).toInt())
                    .toFloat()
            ringSize =
                attributes.getDimensionPixelSize(R.styleable.CircularChart_ringSize, toPx(10).toInt()).toFloat()
            innerCircleRadius = outerCircleRadius - ringSize

            val size = attributes.getDimensionPixelSize(R.styleable.CircularChart_textSize, -1)
            if (size != -1) {
                textPaint.textSize = size.toFloat()
            } else {
                textPaint.textSize = toPx(10)
            }

            isCurve = attributes.getBoolean(R.styleable.CircularChart_isCurve, true)

            startAngle = attributes.getInteger(R.styleable.CircularChart_startAngle, 0)

            clockwise = attributes.getBoolean(R.styleable.CircularChart_clockwise, true)

            attributes.recycle()

        }

        textPaint.color = arcPaint.color
        //textPaint.typeface = ... todo set typeface here


    }

    fun animateToProgress(pr: Float) {

        val valueAnimator = ValueAnimator.ofInt(0, pr.toInt())
        valueAnimator.duration = 500
        valueAnimator.addUpdateListener {
            progress = (it.animatedValue as Int).toFloat()
            invalidate()
        }
        valueAnimator.start()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        Log.d("Spec", MeasureSpec.toString(widthMeasureSpec))

        var width = 0
        var height = 0

        //Must be this size
        when (widthMode) {
            View.MeasureSpec.EXACTLY -> //Must be this size
                width = widthSize
            View.MeasureSpec.AT_MOST -> //wrap_content
                width = 2 * outerCircleRadius.toInt() + paddingLeft + paddingRight
            View.MeasureSpec.UNSPECIFIED -> //if inside scrollView
                width = widthSize
        }

        //Measure Height
        when (heightMode) {
            View.MeasureSpec.EXACTLY -> //Must be this size
                height = heightSize
            View.MeasureSpec.AT_MOST -> //wrap_content
                height = 2 * outerCircleRadius.toInt() + paddingTop + paddingBottom
            View.MeasureSpec.UNSPECIFIED -> //if inside scrollView
                height = heightSize
        }

        width = Math.min(width, height)
        height = width

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        if (visibility != VISIBLE) {
            return
        }

        //outer circle
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), outerCircleRadius, outerCirclePaint)

        //inner circle
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), innerCircleRadius, innerCirclePaint)

        //arc
        outerOval.left = width / 2 - outerCircleRadius
        outerOval.top = height / 2 - outerCircleRadius
        outerOval.right = width / 2 + outerCircleRadius
        outerOval.bottom = height / 2 + outerCircleRadius

        innerOval.left = width / 2 - innerCircleRadius
        innerOval.top = height / 2 - innerCircleRadius
        innerOval.right = width / 2 + innerCircleRadius
        innerOval.bottom = height / 2 + innerCircleRadius

        var angle = progress / 100 * 360
        if (clockwise) {
            angle = -angle
        }
        path.reset()
        path.arcTo(outerOval, -startAngle.toFloat(), -angle)
        path.arcTo(innerOval, -angle-startAngle, angle)


        path.close()
        canvas.drawPath(path, arcPaint)

        if (progress > 0 && progress < 100 && isCurve) {

            canvas.save()
            canvas.translate((width / 2).toFloat(), (height / 2).toFloat())

            //add circle at start of the arc
            val circleRadius = (outerCircleRadius - innerCircleRadius) / 2
            val r = (innerCircleRadius + outerCircleRadius) / 2
            var cx = r * Math.cos(-(angle + startAngle) * Math.PI / 180)
            var cy = r * Math.sin(-(angle + startAngle) * Math.PI / 180)

            canvas.drawCircle(cx.toFloat(), cy.toFloat(), circleRadius, arcPaint)


            //add circle at end of the arc
            cx = r * Math.cos(-startAngle * Math.PI / 180)
            cy = r * Math.sin(-startAngle * Math.PI / 180)

            canvas.drawCircle(cx.toFloat(), cy.toFloat(), circleRadius, arcPaint)

            canvas.restore()

        }

        //draw text
        val text = "%" + progress.toInt().toString()
        textPaint.getTextBounds(
            text,
            0,
            text.length,
            textBound
        )
        canvas.drawText(
            "٪" + progress.toInt().toString(),
            (width / 2).toFloat() - textBound.width() / 2,
            (height / 2).toFloat() - (textPaint.descent() + textPaint.ascent()) / 2,
            textPaint
        )
    }
}