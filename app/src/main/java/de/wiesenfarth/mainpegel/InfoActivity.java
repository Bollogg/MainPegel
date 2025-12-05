package de.wiesenfarth.mainpegel;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;


public class InfoActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.info_activity);

    // --------------------------------------------------------
    // Toolbar einrichten
    // --------------------------------------------------------
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(R.string.menu_info);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
