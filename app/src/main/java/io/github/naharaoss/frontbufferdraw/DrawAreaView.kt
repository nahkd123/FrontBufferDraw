package io.github.naharaoss.frontbufferdraw

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.graphics.lowlatency.GLFrontBufferedRenderer

class DrawAreaView(context: Context) : SurfaceView(context) {
    val renderer = GLFrontBufferedRenderer(this, DrawAreaRenderer())
    private var lastInput: PenInput? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        val input = PenInput(event.x, event.y, event.pressure)
        val lastInput = this.lastInput ?: input
        this.lastInput = input

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                requestUnbufferedDispatch(event)
                renderer.renderFrontBufferedLayer(input to input)
            }
            MotionEvent.ACTION_MOVE -> {
                renderer.renderFrontBufferedLayer(lastInput to input)
            }
            MotionEvent.ACTION_UP -> {
                renderer.renderFrontBufferedLayer(lastInput to input)
                renderer.commit()
            }
            MotionEvent.ACTION_CANCEL -> {
                renderer.cancel()
            }
        }

        return true
    }
}