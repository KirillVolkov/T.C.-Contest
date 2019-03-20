package com.telegram.tgc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telegram.tgc.ChartAdapter.Companion.ITEM_EXTRA
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.item_chart.view.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        rv_charts.layoutManager = LinearLayoutManager(this)
        rv_charts.adapter = ChartAdapter {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra(ITEM_EXTRA, it)
            })
        }
    }
}

class ChartAdapter(val clickDelegate: (Int) -> Unit) : RecyclerView.Adapter<ChartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        return ChartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_chart,
                parent,
                false
            ),
            clickDelegate
        )
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        holder.bind(position)
    }

    companion object {
        const val ITEM_EXTRA = "item"
    }

}

class ChartViewHolder(itemView: View, clickDelegate: (Int) -> Unit) :
    RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener {
            clickDelegate(adapterPosition)
        }
    }

    fun bind(item: Int) {
        itemView.tv_count.text = "Difficulty: ${item}"
    }
}