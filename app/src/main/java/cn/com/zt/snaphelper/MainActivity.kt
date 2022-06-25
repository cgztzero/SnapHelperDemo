package cn.com.zt.snaphelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.*
import cn.com.zt.snaphelper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    companion object{
        val TAG = MainActivity.javaClass.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val wm = this.windowManager;
        val width = wm.getDefaultDisplay().getWidth()
        val padding = width / 2 - 100
        activityMainBinding.recyclerView.setPadding(padding, 0, padding, 0)

        val layoutManager = SmoothScrollerLinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL, false
        )
        layoutManager.setMillisSecond(120f)
        activityMainBinding.recyclerView.layoutManager = layoutManager

        val adapter = SquareAdapter(this)
        adapter.setItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.e(TAG, "position:$position")
                activityMainBinding.recyclerView.smoothScrollToPosition(position)
            }

        })
        activityMainBinding.recyclerView.adapter = adapter
        activityMainBinding.button.setOnClickListener {
            activityMainBinding.recyclerView.smoothScrollToPosition(5)
        }

        val snapHelper = CustomerSnapHelper()
        snapHelper.attachToRecyclerView(activityMainBinding.recyclerView)
        snapHelper.setItemSelectListener(object : ItemSelectedListener {
            override fun onItemSelected(view: View?, position: Int) {
                Log.e(TAG, "item选择$position")
            }
        })

    }
}