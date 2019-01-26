package com.hm60.circularchart

import android.util.TypedValue
import android.view.View

fun View.toPx(dp: Int):Float{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    )
}