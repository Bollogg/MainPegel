package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.net.wiesenfarth.mainpegel.databinding.ActivityDatenschutzerklaerungBinding

class DatenschutzerklaerungActivity : AppCompatActivity() {

	private lateinit var binding: ActivityDatenschutzerklaerungBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityDatenschutzerklaerungBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)
		binding.toolbar.title = "Datenschutzerklärung"
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		val text = assets.open("datenschutzerklaerung.txt")
			.bufferedReader()
			.use { it.readText() }

		binding.privacyText.text = text
	}

	override fun onSupportNavigateUp(): Boolean {
		finish()
		return true
	}
}
