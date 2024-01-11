package za.co.grab.yaldp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.lang.Math.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

class LidarView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var angle = 0f
    private var radius = 0f
    private var data: String = ""
    private var TAG: String = "YALDP"
    val list = ArrayList<Pair<Int, Int>>()
    private var count: Int = 0
    private var minDistance: Float = 10000.00f
    private var maxDistance: Float = 0.0f
    private var socketAngle: String = "0.0"
    private var socketLength: String = "0.0"
    private var socketBit: String = "0.0"
    private var socketQuality: String = "0.0"
    private var status: String = "Disconnected"

    private val circleLight = Paint().apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val circle = Paint().apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val pointColour = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 10f
        isAntiAlias = true
    }

    private val lineColor = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val textLayout = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 48f
    }

    private var sliderValue = 9f
    private var sliderPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    fun setData(data: String) {
        this.data = data
    }

    // Define a handler and runnable for updating the angle of the line
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            angle = 20f // increment the angle by 6 degrees (1 second)
            radius = width.coerceAtMost(height / 400)
                .toFloat() // calculate the radius based on the size of the view
            //Log.w(TAG, "Radius: $radius")
            invalidate() // redraw the view
            handler.postDelayed(this, 1) // schedule the runnable to run after 1 second
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Get the center coordinates of the view
        val centerX = width / 2f
        val centerY = height / 2f
        val canvasWidth = width
        val canvasHeight = height

        // Draw the circle
        val circleRadius = centerX.coerceAtMost(centerY) * 1f
        canvas.drawCircle(centerX, centerY, circleRadius, circle)

        Log.w(TAG, "Data: $data")

//        if (inData.isNotEmpty()) {
//            //Log.e(TAG, "RawData: " + inData)
//            val myArray = inData.replace("[", "").replace("]", "").split(",").toTypedArray()
//
//            for (value in myArray) {
//
//                //Get max and min Values
//                if (value.trim().toFloat() > maxDistance) {
//                    maxDistance = value.trim().toFloat()
//                }
//
//                minDistance = value.trim().toFloat()
//
//                //Log.w(TAG, "Data: " + count + " - " + value.trim())
//                val lineX = centerX + (radius * -(value.trim().toFloat() / sliderValue)) * cos(Math.toRadians(count.toDouble())).toFloat()
//                val lineY = (centerY) + (radius * -(value.trim().toFloat() / sliderValue)) * sin(Math.toRadians(count.toDouble())).toFloat()
//
//                // Draw the line that fills the circle
//                canvas.drawLine(centerX, centerY, lineX, lineY, lineColor)
//                canvas.drawPoint(lineX, lineY, pointColour)
//                count++
//            }
//            count = 0
//        }

        //Cross hairs on top of drawing
        canvas.drawLine(centerX.toFloat(), 460f, centerX.toFloat(), canvasHeight.toFloat() - 460f, circle)
        canvas.drawLine(0f, centerY.toFloat(), canvasWidth.toFloat(), centerY.toFloat(), circle)

        minDistance = (minDistance / 1000.0).toFloat()
        val minDistanceStat = String.format("%.2f", minDistance)

        maxDistance = (maxDistance / 1000.0).toFloat()
        val maxDistanceStat = String.format("%.2f", maxDistance)

        canvas.drawText("Min Distance:   $minDistanceStat m", 20f, 100f, textLayout)
        canvas.drawText("Max Distance:  $maxDistanceStat m", 20f, 170f, textLayout)
        canvas.drawText("Status:  $status", 20f, height - 50f, textLayout)

        val circleRadiusb = centerX.coerceAtMost(centerY) * .7f
        canvas.drawCircle(centerX, centerY, circleRadiusb, circleLight)
        val circleRadiusc = centerX.coerceAtMost(centerY) * .5f
        canvas.drawCircle(centerX, centerY, circleRadiusc, circleLight)
        val circleRadiusd = centerX.coerceAtMost(centerY) * .3f
        canvas.drawCircle(centerX, centerY, circleRadiusd, circleLight)
        val circleRadiuse = centerX.coerceAtMost(centerY) * .0f
        canvas.drawCircle(centerX, centerY, circleRadiuse, circleLight)

//        val sliderX = (sliderValue / 20f) * width
//        canvas.drawLine(10f, height - 100f, width.toFloat(), height -100f, sliderPaint)
//        canvas.drawCircle(sliderX, height - 100f, 30f, sliderPaint)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN || event?.action == MotionEvent.ACTION_MOVE) {
            sliderValue = (event?.x ?: 0f) / width * 20f
            Log.w(TAG, "Slide: $sliderValue")
            invalidate()
            return true
        }
        return false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(runnable) // start the runnable when the view is attached to the window
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(runnable) // stop the runnable when the view is detached from the window
    }
}