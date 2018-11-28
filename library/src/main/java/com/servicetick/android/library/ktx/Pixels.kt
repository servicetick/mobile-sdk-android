package com.servicetick.android.library.ktx

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

fun DisplayMetrics.toDp(px: Int): Float = density * px

/**
 * Applies density dimensions to the [Int]
 */
fun Int.toDp(displayMetrics: DisplayMetrics) = toFloat().toDp(displayMetrics).toInt()

fun Int.toDp(resources: Resources) = toFloat().toDp(resources.displayMetrics).toInt()

/**
 * Applies density dimensions to the [Float]
 */
fun Float.toDp(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)

fun Float.toDp(resources: Resources) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)