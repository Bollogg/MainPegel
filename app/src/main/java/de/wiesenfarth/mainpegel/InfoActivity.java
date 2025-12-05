package de.wiesenfarth.mainpegel;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import de.wiesenfarth.mainpegel.ui.main.InfoFragment;

public class InfoActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_info);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.container, InfoFragment.newInstance())
          .commitNow();
    }
  }
}