package de.wiesenfarth.mainpegel

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)

        // --------------------------------------------------------
        // Toolbar einrichten
        // --------------------------------------------------------
        val toolbar: Toolbar?
        Toolbar > < android . view . View > findViewById < android . view . View ? > (R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setTitle(R.string.menu_info)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
    }

    /*
  getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
    @Override
    public void handleOnBackPressed() {
      finish();
    }
  });
*/
    // --------------------------------------------------------
    // Toolbar-Pfeil zurück → Einstellungen speichern
    // --------------------------------------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
