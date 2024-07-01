package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt

class GetColorFromAttrUseCase {

    @ColorInt
    operator fun invoke(attr: Int, context: Context): Int {
            val tv = TypedValue()
            context.theme.resolveAttribute(attr, tv, true)
            return context.resources.getColor(tv.resourceId)
    }
}