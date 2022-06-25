package cn.com.zt.snaphelper

import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.abs
import kotlin.math.roundToInt

class CustomerSnapHelper : SnapHelper() {
    companion object {
        val INVALID_DISTANCE = 1f
    }

    private var mVerticalHelper: OrientationHelper? = null

    private var mHorizontalHelper: OrientationHelper? = null

    private var mCurrentRecyclerView: RecyclerView? = null

    private var mItemSelectListener: ItemSelectedListener? = null

    fun setItemSelectListener(l: ItemSelectedListener) {
        this.mItemSelectListener = l
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(
                targetView,
                getHorizontalHelper(layoutManager)!!
            )
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(
                 targetView, getVerticalHelper(layoutManager)!!
            )
        }

        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager)!!)
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager)!!)
        }

        return null
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }

        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider

        val vectorForEnd: PointF =
            vectorProvider.computeScrollVectorForPosition(itemCount - 1) ?: return RecyclerView.NO_POSITION

        var vDeltaJump = 0
        var hDeltaJump = 0

        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(
                layoutManager,
                getHorizontalHelper(layoutManager)!!,
                velocityX, 0
            )
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump
            }
        }

        if (layoutManager.canScrollVertically()) {
            vDeltaJump = estimateNextPositionDiffForFling(
                layoutManager,
                getVerticalHelper(layoutManager)!!,
                0, velocityY
            )
            if (vectorForEnd.y < 0) {
                vDeltaJump = -vDeltaJump
            }
        }

        val deltaJump = if (layoutManager.canScrollVertically()) {
            vDeltaJump
        } else {
            hDeltaJump
        }

        var targetPos = currentPosition + deltaJump
        if (targetPos < 0) {
            targetPos = 0
        } else if (targetPos >= itemCount) {
            targetPos = itemCount - 1
        }

        return targetPos
    }

    private fun estimateNextPositionDiffForFling(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper, velocityX: Int, velocityY: Int
    ): Int {
        val distances = calculateScrollDistance(velocityX, velocityY)
        val distancePerChild: Float = computeDistancePerChild(layoutManager, helper)
        if (distancePerChild <= 0) {
            return 0
        }
        val distance = if (abs(distances[0]) > abs(distances[1])) distances[0] else distances[1]
        return (distance / distancePerChild).roundToInt()
    }

    private fun computeDistancePerChild(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MAX_VALUE
        var maxPos = Int.MIN_VALUE
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return INVALID_DISTANCE
        }
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val pos = layoutManager.getPosition(child!!)
            if (pos == RecyclerView.NO_POSITION) {
                continue
            }
            if (pos < minPos) {
                minPos = pos
                minPosView = child
            }
            if (pos > maxPos) {
                maxPos = pos
                maxPosView = child
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE
        }
        val start = helper.getDecoratedStart(minPosView).coerceAtMost(helper.getDecoratedStart(maxPosView))
        val end = helper.getDecoratedEnd(minPosView).coerceAtLeast(helper.getDecoratedEnd(maxPosView))
        val distance = end - start
        return if (distance == 0) {
            INVALID_DISTANCE
        } else 1f * distance / (maxPos - minPos + 1)
    }

    private fun distanceToCenter(
        targetView: View, helper: OrientationHelper
    ): Int {
        val childCenter = (helper.getDecoratedStart(targetView)
                + helper.getDecoratedMeasurement(targetView) / 2)
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun getHorizontalHelper(
        layoutManager: RecyclerView.LayoutManager
    ): OrientationHelper? {
        if (mHorizontalHelper == null || mHorizontalHelper!!.layoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if (mVerticalHelper == null || mVerticalHelper!!.layoutManager !== layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper
    }

    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = (helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2)
            val absDistance = abs(childCenter - center)
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        if (mCurrentRecyclerView?.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            mItemSelectListener?.onItemSelected(
                closestChild,
                layoutManager.getPosition(closestChild!!)
            )
        }
        return closestChild
    }

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        mCurrentRecyclerView = recyclerView
    }
}

interface ItemSelectedListener {
    fun onItemSelected(view: View?, position: Int)
}