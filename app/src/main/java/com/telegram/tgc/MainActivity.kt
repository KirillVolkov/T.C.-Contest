package com.telegram.tgc

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
        holder.bind(items[position])
    }
}

class SetViewHolder(itemView: View, clickDelegate: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener {
            clickDelegate(adapterPosition)
        }
    }

    fun bind(item: ChartDataSetCheckWrapper) {
        itemView.tv_name.text = item.dataset.axisName
        itemView.iv_checkmark.circleColorResId = item.dataset.color
        itemView.iv_checkmark.checked = item.checked
    }
}
