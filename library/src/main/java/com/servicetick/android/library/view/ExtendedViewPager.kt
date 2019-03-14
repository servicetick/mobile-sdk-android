package com.servicetick.android.library.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.RestrictTo
import androidx.core.content.withStyledAttributes
import androidx.viewpager.widget.ViewPager
import com.servicetick.android.library.R

/**
 * ViewPager with disable swipe functionality
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ExtendedViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private var disableSwipe: Boolean = false

    init {
        init(attrs, R.attr.ExtendedViewPagerStyle)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {

        context.withStyledAttributes(attrs, R.styleable.ExtendedViewPager, defStyleAttr, 0) {
            disableSwipe = getBoolean(R.styleable.ExtendedViewPager_disableSwipe, false)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return !disableSwipe && super.onTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return !disableSwipe && super.onInterceptTouchEvent(ev)
    }

    fun setDisableSwipe(disableSwipe: Boolean) {
        this.disableSwipe = disableSwipe
    }
}
