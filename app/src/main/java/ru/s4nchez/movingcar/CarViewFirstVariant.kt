package ru.s4nchez.movingcar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator

class CarViewFirstVariant(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        private const val DESTINATION_MARKER_RADIUS = 50.0f
        private const val CAR_MARKER_WIDTH = 100.0f
        private const val CAR_MARKER_HEIGHT = 175.0f
        private const val DESTINATION_POINT_START_X = 500f
        private const val DESTINATION_POINT_START_Y = -200f
        private const val CAR_POINT_START_X = 500f
        private const val CAR_POINT_START_Y = -1000f
        private const val CAR_POINT_START_ANGLE = 0f
    }

    private var destinationPoint = PointF(DESTINATION_POINT_START_X, DESTINATION_POINT_START_Y)

    private val destinationPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
    private val carPointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val carMatrix = Matrix()
    private var carBitmap: Bitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(context.resources, R.drawable.car),
            CAR_MARKER_WIDTH.toInt(), CAR_MARKER_HEIGHT.toInt(), false)

    private val car = Car(
            x = CAR_POINT_START_X,
            y = CAR_POINT_START_Y,
            currentAngle = CAR_POINT_START_ANGLE,
            neededAngle = CAR_POINT_START_ANGLE
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
        drawCar(canvas)
    }

    private fun drawDestinationPoint(canvas: Canvas) {
        if (car.isRotating || car.isMoving) {
            canvas.drawCircle(
                    destinationPoint.x,
                    destinationPoint.y,
                    DESTINATION_MARKER_RADIUS,
                    destinationPointPaint)
        }
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
        if (car.isMoving || car.isRotating) {
            return
        }

        destinationPoint.x = x
        destinationPoint.y = y

        val extraPoint = PointF(car.x, y)

        val cathet1Length = Math.abs(car.y - extraPoint.y).toDouble()
        val cathet2Length = Math.abs(destinationPoint.x - extraPoint.x).toDouble()
        val atan = Math.atan(cathet2Length / cathet1Length)
        val triangleAngle = Math.toDegrees(atan)

        val destinationAngle = when {
            destinationPoint.y >= car.y && destinationPoint.x >= car.x -> 180.0 - triangleAngle
            destinationPoint.y >= car.y && destinationPoint.x < car.x -> 180.0 + triangleAngle
            destinationPoint.y < car.y && destinationPoint.x >= car.x -> triangleAngle
            else -> -triangleAngle
        }.toFloat()

        car.neededAngle = calculateNeededAngle(destinationAngle)

        car.startRotate()
        ValueAnimator.ofFloat(car.currentAngle, car.neededAngle).apply {
            duration = 1000
            interpolator = LinearInterpolator()
            addUpdateListener {
                car.currentAngle = (it.animatedValue as Float)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    startMoving()
                    super.onAnimationEnd(animation)
                }
            })
        }.start()

        invalidate()
    }

    private fun calculateNeededAngle(destinationAngle: Float): Float {
        val diff = Math.abs(car.currentAngle - destinationAngle)
        return if (diff > 180f) {
            if (destinationAngle > car.currentAngle) {
                car.currentAngle - (360 - diff)
            } else {
                car.currentAngle + (360 - diff)
            }
        } else {
            destinationAngle
        }
    }

    private fun startMoving() {
        car.startMoving()

        val path = Path()
        path.moveTo(car.x, car.y)
        path.lineTo(destinationPoint.x, destinationPoint.y)
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)

        val pathLength = pathMeasure.length
        ValueAnimator.ofFloat(0f, pathLength).apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                val pos = FloatArray(2)
                val tan = FloatArray(2)
                pathMeasure.getPosTan(value, pos, tan)
                car.x = pos[0]
                car.y = pos[1]
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    car.stopMoving()
                    super.onAnimationEnd(animation)
                }
            })
        }.start()
    }

    class Car(
            var x: Float,
            var y: Float,
            var currentAngle: Float,
            var neededAngle: Float) {

        var isRotating: Boolean = false
            private set

        var isMoving: Boolean = false
            private set

        fun startRotate() {
            isRotating = true
            isMoving = false
        }

        fun startMoving() {
            isRotating = false
            isMoving = true
            calculateRightAngle()
        }

        fun stopMoving() {
            isRotating = false
            isMoving = false
        }

        private fun calculateRightAngle() {
            if (currentAngle < -90) {
                val tmp = -90 - currentAngle
                currentAngle = 270 - tmp
            } else if (currentAngle > 270) {
                val tmp = currentAngle - 270
                currentAngle = -90 + tmp
            }
        }
    }
}