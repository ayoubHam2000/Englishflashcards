package com.example.englishflashcards.Classes.Costum

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager


class MyViewPage : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var disable = true
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return !disable && super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return !disable && super.onTouchEvent(event)
    }

    fun disableScroll(disable: Boolean) {
        //When disable = true not work the scroll and when disble = false work the scroll
        this.disable = disable
    }

}
