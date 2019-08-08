package ru.s4nchez.movingcar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

class CarViewFirstVariant(context: Context, attrs: AttributeSet?) : View(context, attrs) {

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

    private var destinationPoint = PointF(200.0f, 500.0f)
    private var extraPoint = PointF(0.0f, 0.0f)

    private val destinationPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLUE }
    private val extraPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GREEN }
    private val carPointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val carMatrix = Matrix()
    private var carBitmap: Bitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(context.resources, R.drawable.car),
            CAR_MARKER_WIDTH.toInt(), CAR_MARKER_HEIGHT.toInt(), false)

    private val car = Car(
            x = 500f,
            y = 500f,
            currentAngle = 0.0f,
            neededAngle = 0.0f
    )

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
        drawDestinationPoint(canvas)
        drawExtraPoint(canvas)
        drawCar(canvas)
    }

    private fun drawExtraPoint(canvas: Canvas) {
        canvas.drawCircle(
                extraPoint.x,
                extraPoint.y,
                DESTINATION_MARKER_RADIUS / 3,
                extraPointPaint)
    }

    private fun drawDestinationPoint(canvas: Canvas) {
        canvas.drawCircle(
                destinationPoint.x,
                destinationPoint.y,
                DESTINATION_MARKER_RADIUS,
                destinationPointPaint)
    }

    private fun drawCar(canvas: Canvas) {
        val carCenterX = car.x - CAR_MARKER_WIDTH / 2
        val carCenterY = car.y - CAR_MARKER_HEIGHT / 2

        carMatrix.reset()
        carMatrix.postTranslate(carCenterX, carCenterY)
        carMatrix.postRotate(car.currentAngle, car.x, car.y)

        canvas.drawBitmap(carBitmap, carMatrix, carPointPaint)
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
        destinationPoint.x = x
        destinationPoint.y = y

        extraPoint.x = car.x
        extraPoint.y = y

        val cathet1Length = Math.abs(car.y - extraPoint.y).toDouble()
        val cathet2Length = Math.abs(destinationPoint.x - extraPoint.x).toDouble()
        val atan = Math.atan(cathet2Length / cathet1Length)
        val triangleAngle = Math.toDegrees(atan)

        car.neededAngle = when {
            destinationPoint.y >= car.y && destinationPoint.x >= car.x -> 180.0 - triangleAngle
            destinationPoint.y >= car.y && destinationPoint.x < car.x -> 180.0 + triangleAngle
            destinationPoint.y < car.y && destinationPoint.x >= car.x -> triangleAngle
            else -> -triangleAngle
        }.toFloat()

        car.isRotating = true
        val animator = ValueAnimator.ofFloat(car.currentAngle, car.neededAngle)
        animator.duration = 1000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            car.currentAngle = (it.animatedValue as Float)
            invalidate()
        }
        animator.start()

        Log.d("sssss", car.neededAngle.toString())

//        Log.d("sssss", "cathet 1: $cathet1Length")
//        Log.d("sssss", "cathet 2: $cathet2Length")
//        Log.d("sssss", "hz: ${Math.toDegrees(triangleAngle)}")

        invalidate()
    }

    class Car(
            var x: Float,
            var y: Float,
            var currentAngle: Float,
            var neededAngle: Float,
            var isRotating: Boolean = false,
            var isMoving: Boolean = false
    )
}