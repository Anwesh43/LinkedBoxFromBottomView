package com.anwesh.uiprojects.boxfrombottomview

/**
 * Created by anweshmishra on 16/10/20.
 */

import android.view.View
import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#673AB7",
        "#4CAF50",
        "#03A9F4",
        "#009688"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 4
val scGap : Float = 0.02f / parts
val barFactor : Float = 5.6f
val pipeFactor : Float = 13.2f
val delay : Long = 20
val strokeFactor : Float = 90f
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBoxFromBottom(scale : Float, w : Float, h : Float, paint : Paint) {
    val barSize : Float = Math.min(w, h) / barFactor
    val pipeSize : Float = Math.min(w, h) / pipeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val pipeW : Float = (w - 2 * barSize) * sf3
    save()
    translate(0f, h / 2)
    for (j in 0..1) {
        val bSize : Float = barSize * sf1
        save()
        translate(barSize + (w - 2 * barSize), h * 0.5f - barSize - (h / 2) * sf2)
        drawRect(RectF(-bSize / 2, -bSize / 2, bSize, bSize), paint)
        restore()
    }
    save()
    translate(w / 2, 0f)
    drawRect(RectF(-pipeW / 2, -pipeSize / 2, pipeW / 2, pipeSize / 2), paint)
    restore()
    restore()
}

fun Canvas.drawBFBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawBoxFromBottom(scale, w, h, paint)
}

class BoxFromBottomView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BFBNode(var i : Int, val state : State = State()) {

        private var next : BFBNode? = null
        private var prev : BFBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BFBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBFBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BFBNode {
            var curr : BFBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BoxFromBottom(var i : Int) {

        private var curr : BFBNode = BFBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BoxFromBottomView) {

        private val animator : Animator = Animator(view)
        private val bfb : BoxFromBottom = BoxFromBottom(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bfb.draw(canvas, paint)
            animator.animate {
                bfb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bfb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BoxFromBottomView {
            val view : BoxFromBottomView = BoxFromBottomView(activity)
            activity.setContentView(view)
            return view
        }
    }
}