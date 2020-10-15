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
