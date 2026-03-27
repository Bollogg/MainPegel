package de.net.wiesenfarth.mainpegel.DataBase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.net.wiesenfarth.mainpegel.R

class WaterAdapter(private val list: List<RowData>) :
	RecyclerView.Adapter<WaterAdapter.ViewHolder>() {

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val time: TextView = view.findViewById(R.id.tvTime)
		val value: TextView = view.findViewById(R.id.tvValue)
		val temp: TextView = view.findViewById(R.id.tvTemp)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.row_water, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount() = list.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = list[position]

		holder.time.text = item.time
		holder.value.text = "${item.value} cm"

		holder.temp.text =
			if (item.temp.isNaN()) "--"
			else "${item.temp}°C"
	}
}