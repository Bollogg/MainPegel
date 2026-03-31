package de.net.wiesenfarth.mainpegel.DataBase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.net.wiesenfarth.mainpegel.MoonPhase.MoonPhase
import de.net.wiesenfarth.mainpegel.R
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WaterAdapter(private val list: List<RowData>) :
	RecyclerView.Adapter<WaterAdapter.ViewHolder>() {

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val time: TextView = view.findViewById(R.id.tvTime)
		val value: TextView = view.findViewById(R.id.tvValue)
		val temp: TextView = view.findViewById(R.id.tvTemp)
		val moon: TextView = view.findViewById(R.id.tvMoon)
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

		// ISO-Datum formatieren
		val isoDate = try {
			val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

			val localDateTime = java.time.LocalDateTime.parse(item.time, inputFormatter)

			// ISO-String erzeugen
			val iso = localDateTime
				.atZone(java.time.ZoneId.systemDefault())
				.toOffsetDateTime()
				.toString()

			iso

		} catch (e: Exception) {
			"--"
		}		// 🌙 Mondphase (ISO direkt!)
		val moonText = try {
			val phase = MoonPhase.getMoonPhase(isoDate)

			holder.itemView.context.getString(
				MoonPhase.getMoonPhaseStringRes(phase)
			)
		} catch (e: Exception) {
			"-"
		}

		holder.moon.text = moonText

	}
}