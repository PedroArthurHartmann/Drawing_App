package com.example.drawingapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var mDrawPath: CustomPath //internal inner class
    private lateinit var mCanvasBitmap: Bitmap //the bitmap of the canvas
    private lateinit var mCanvasPaint: Paint //
    private lateinit var mDrawPaint: Paint //instance of the canvas paint view
    private lateinit var mCanvas: Canvas //the canvas in which everything is going to be drawn on
    private var mColor = Color.BLACK
    private var mBrushSize = 0f
    private val mPaths = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(mColor, mBrushSize)
        mDrawPaint.color = mColor
        mDrawPaint.style = Paint.Style.STROKE
        mDrawPaint.strokeJoin = Paint.Join.ROUND
        mDrawPaint.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint.strokeWidth = path.brushThickness
            mDrawPaint.color = path.color
            canvas?.drawPath(path, mDrawPaint)
        }

        if (!mDrawPath.isEmpty) {
            mDrawPaint.strokeWidth = mDrawPath.brushThickness
            mDrawPaint.color = mDrawPath.color
            canvas?.drawPath(mDrawPath, mDrawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath.color = mColor
                mDrawPath.brushThickness = mBrushSize

                mDrawPath.reset()
                mDrawPath.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> mDrawPath.lineTo(event.x, event.y)
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath)
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    fun setSizeForBrush(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPaint.color = mColor
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}