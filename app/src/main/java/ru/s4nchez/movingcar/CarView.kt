package ru.s4nchez.movingcar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

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

    private var isTaxiportation = false

    private var bezierPath = Path()
    private var bezierPathMeasure = PathMeasure()
    private val bezierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

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

    private val testPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 30f
        color = Color.GREEN
    }
    private val testPoint = Point(0f, 0f)

    override fun onDraw(canvas: Canvas) {
        drawBezier(canvas)
        canvas.drawCircle(
                destinationMarkerPoint.x,
                destinationMarkerPoint.y,
                DESTINATION_MARKER_RADIUS,
                destinationMarkerPaint)
        drawCar(canvas)
        canvas.drawPoint(testPoint.x, testPoint.y, testPaint)
    }

    private fun drawBezier(canvas: Canvas) {
        val carCenterX = carMarkerPoint.x
        val carCenterY = carMarkerPoint.y

        val extraPointX = 1000f
        val extraPointY = 1000f

        bezierPath.reset()
        bezierPath.moveTo(carCenterX, carCenterY)
        bezierPath.quadTo(extraPointX, extraPointY, destinationMarkerPoint.x, destinationMarkerPoint.y)

        canvas.drawPath(bezierPath, bezierPaint)
    }

    private fun drawCar(canvas: Canvas) {
        val carCenterX = getCarCenterX()
        val carCenterY = getCarCenterY()

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
                performClick()
                handleClick(event.x, event.y)
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

        isTaxiportation = true

        val path = Path()
        val carCenterX = carMarkerPoint.x
        val carCenterY = carMarkerPoint.y
        val extraPointX = 1000f
        val extraPointY = 1000f
        path.reset()
        path.moveTo(carCenterX, carCenterY)
        path.quadTo(extraPointX, extraPointY, destinationMarkerPoint.x, destinationMarkerPoint.y)

        bezierPathMeasure.setPath(path, false)
        val pathLength = bezierPathMeasure.length

        val animator = ValueAnimator.ofFloat(0.0f, pathLength)
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {

            val value = it.animatedValue as Float
            l(value)

            bezierPathMeasure.setPath(path, false)
            val matrix = Matrix()
            bezierPathMeasure.getMatrix(value, matrix, PathMeasure.POSITION_MATRIX_FLAG + PathMeasure.TANGENT_MATRIX_FLAG)
            val pos = FloatArray(2)
            val tan = FloatArray(2)
            bezierPathMeasure.getPosTan(value, pos, tan)

            carMarkerPoint.x = pos[0]
            carMarkerPoint.y = pos[1]
            invalidate()
        }
        animator.start()

        invalidate()
    }

    private fun getCarCenterX() = carMarkerPoint.x - CAR_MARKER_WIDTH / 2

    private fun getCarCenterY() = carMarkerPoint.y - CAR_MARKER_HEIGHT / 2

    open class Point(var x: Float, var y: Float)

    class CarPoint(x: Float, y: Float, var angle: Float) : Point(x, y)

    private fun l(value: Any?) {
        Log.d("sssss", value?.toString() ?: "")
    }
}