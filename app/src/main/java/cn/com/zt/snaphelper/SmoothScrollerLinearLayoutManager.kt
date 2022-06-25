package cn.com.zt.snaphelper

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class SmoothScrollerLinearLayoutManager: LinearLayoutManager {
    var milliSecond = 25f

    fun setMillisSecond(time: Float) {
        this.milliSecond = time
    }

    constructor(cxt: Context, direction: Int, reverse: Boolean) : super(cxt, direction, reverse)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val smoothScroller = SmoothScroller(milliSecond, recyclerView.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
    }
}

class SmoothScroller(time: Float = 25f, cxt: Context) : LinearSmoothScroller(cxt) {
    var milliSecond = time

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return milliSecond / displayMetrics.densityDpi
    }

}