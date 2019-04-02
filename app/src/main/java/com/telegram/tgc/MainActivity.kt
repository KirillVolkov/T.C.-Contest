package com.telegram.tgc

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.telegram.tgc.ChartAdapter.Companion.ITEM_EXTRA
import com.telegram.tgc.model.ChartAxisData
import com.telegram.tgc.model.ChartDataSet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.control_list_item.view.*

class MainActivity : AppCompatActivity() {

    lateinit var adapter: SetsAdapter

    var nightModeEnabled = false

    val nightColorSet =
        ColorSet(
            Color.parseColor("#161d26"),
            Color.parseColor("#19212d"),
            Color.parseColor("#11171d"),
            Color.WHITE
        )

    val dayColorSet = ColorSet(Color.LTGRAY, Color.WHITE, Color.LTGRAY, Color.BLACK)

    override fun onCreate(savedInstanceState: Bundle?) {

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sets = parseData(resources)
        val set = sets[intent.getIntExtra(ITEM_EXTRA, 0)]
        chart_view.setData(set)

        adapter =
            SetsAdapter(set.columns.filter { it.axisName != "x" }.map { ChartDataSetCheckWrapper(it) }, ::clickCallback)
        rv_controls.adapter = adapter

        ibt_switch_theme.setOnClickListener {
            nightModeEnabled = !nightModeEnabled
            val currset = if (nightModeEnabled) nightColorSet else dayColorSet
            toolbar.setBackgroundColor(currset.lightColor)
            toolbar.setTitleTextColor(currset.toastTextColor)
            window.statusBarColor = currset.lightColor
            lyt_main.setBackgroundColor(currset.lightColor)
            rv_controls.setBackgroundColor(currset.lightColor)
            adapter.isNight = nightModeEnabled
            ibt_switch_theme.setImageResource(if (nightModeEnabled) R.drawable.moon_enabled else R.drawable.moon_disabled)
            chart_view.setColorSet(currset)
        }
    }

    fun clickCallback(adapterPosition: Int) {
        chart_view.selectAxis(adapterPosition, adapter.invertCheck(adapterPosition))
    }
}

class ChartDataSetCheckWrapper(val dataset: ChartAxisData) {
    var checked: Boolean = true
}

class SetsAdapter(val items: List<ChartDataSetCheckWrapper>, val clickDelegate: (Int) -> Unit) :
    RecyclerView.Adapter<SetViewHolder>() {

    var isNight: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SetViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.control_list_item, parent, false),
            clickDelegate
        )

    fun invertCheck(adapterPosition: Int): Boolean {
        val currState = items[adapterPosition].checked
        items[adapterPosition].checked = !currState
        notifyItemChanged(adapterPosition)
        return !currState
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(items[position], isNight)
    }
}

class SetViewHolder(itemView: View, clickDelegate: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {



    init {
        itemView.setOnClickListener {
            clickDelegate(adapterPosition)
        }
    }

    fun bind(item: ChartDataSetCheckWrapper, nightTheme: Boolean) {
        itemView.tv_name.text = item.dataset.axisName
        itemView.tv_name.setTextColor(if (nightTheme) Color.WHITE else Color.BLACK)
        itemView.iv_checkmark.circleColorResId = item.dataset.color
        itemView.iv_checkmark.checked = item.checked
    }
}
