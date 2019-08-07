package ru.s4nchez.movingcar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class CarView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        private const val DESTINATION_MARKER_RADIUS = 50.0f
        private const val CAR_MARKER_WIDTH = 100.0f
        private const val CAR_MARKER_HEIGHT = 175.0f

        private const val DESTINATION_MARKER_START_X = 500f
        private const val DESTINATION_MARKER_START_Y = 500f
        private const val CAR_MARKER_START_X = 500f
        private const val CAR_MARKER_START_Y = 500f
        private const val CAR_MARKER_START_ANGLE = 0f
    }

    private val destinationMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLUE }
    private val carMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val destinationMarkerPoint = Point(DESTINATION_MARKER_START_X, DESTINATION_MARKER_START_Y)

    private var carBitmap: Bitmap = Bitmap.createScaledBitmap(BitmapFactory
            .decodeResource(context.resources, R.drawable.car), CAR_MARKER_WIDTH.toInt(), CAR_MARKER_HEIGHT.toInt(), false)

    private val carMarkerPoint = CarPoint(CAR_MARKER_START_X, CAR_MARKER_START_Y, CAR_MARKER_START_ANGLE)
    private val carMatrix = Matrix()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = resolveSize(calculateDesiredWidth(), widthMeasureSpec)
        val measureHeight = resolveSize(calculateDesiredHeight(), heightMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)
    }

    private fun calculateDesiredWidth(): Int {
        return suggestedMinimumWidth + paddingLeft + paddingRight
    }

    private fun calculateDesiredHeight(): Int {
        return suggestedMinimumHeight + paddingTop + paddingBottom
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(
                destinationMarkerPoint.x,
                destinationMarkerPoint.y,
                DESTINATION_MARKER_RADIUS,
                destinationMarkerPaint)
        drawCar(canvas)

        carMarkerPoint.angle += 5
        invalidate()
    }

    private fun drawCar(canvas: Canvas) {
        val carCenterX = carMarkerPoint.x - CAR_MARKER_WIDTH / 2
        val carCenterY = carMarkerPoint.y - CAR_MARKER_HEIGHT / 2

        carMatrix.reset()
        carMatrix.postTranslate(carCenterX, carCenterY)
        carMatrix.postRotate(carMarkerPoint.angle, carMarkerPoint.x, carMarkerPoint.y)

        canvas.drawBitmap(carBitmap, carMatrix, carMarkerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var isEventHandled = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isEventHandled = true
            MotionEvent.ACTION_UP -> {
                handleClick(event.x, event.y)
                performClick()
                isEventHandled = true
            }
            MotionEvent.ACTION_CANCEL -> isEventHandled = true
        }
        return isEventHandled
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleClick(x: Float, y: Float) {
        destinationMarkerPoint.x = x
        destinationMarkerPoint.y = y
        invalidate()
    }

    open class Point(var x: Float, var y: Float)

    class CarPoint(x: Float, y: Float, var angle: Float) : Point(x, y)

    fun l(value: Any?) {
        Log.d("sssss", value?.toString() ?: "")
    }
}