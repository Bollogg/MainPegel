package de.net.wiesenfarth.mainpegel.Widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import de.net.wiesenfarth.mainpegel.API.PegelLogic
import de.net.wiesenfarth.mainpegel.Graph.PegelBitmapGenerator
import de.net.wiesenfarth.mainpegel.MainActivity
import de.net.wiesenfarth.mainpegel.R
import java.lang.Float

/*******************************************************
 * Programm:  PegelWidget
 *
 * Beschreibung:
 * AppWidgetProvider für das Pegel-AppWidget.
 *
 * Diese Klasse ist verantwortlich für:
 * - das Aktualisieren des Widgets bei Systemereignissen
 * - das Reagieren auf manuelle oder geplante Update-Requests
 * - das Ermitteln der Widget-Größe (modern & Fallback)
 * - das Rendern der Pegel-Grafik
 * - das Anzeigen der letzten Messwerte
 *
 * Das Widget kann auch dann aktualisiert werden,
 * wenn die App selbst nicht aktiv ist.
 *
 * Aufgaben:
 * - Widget-UI aufbauen (RemoteViews)
 * - Bitmap-Grafik erzeugen
 * - Klick auf Widget → App starten
 * - Update-Zyklen planen
 *
 * Abhängigkeiten:
 * - PegelLogic (API / Datenabruf)
 * - PegelBitmapGenerator (Grafik)
 * - SharedPreferences ("pegel_cache", "settings")
 *
 * Autor:     Bollogg
 * Datum:     2025-11-17
 *******************************************************/
class PegelWidget : AppWidgetProvider() {
	/**
 * Wird vom System aufgerufen, wenn ein Widget-Update nötig ist
 * (z.B. nach dem Hinzufügen des Widgets oder bei Intervallen).
 */

	override fun onUpdate(context: Context, mgr: AppWidgetManager, appWidgetIds: IntArray) {
    for (id in appWidgetIds) {
      Log.i("WIDGET", "updateWidget() CALLER = onUpdate()")
      updateWidget(context, mgr, id)
    }

		// Optional: automatischen Update-Zyklus starten
		// ToDo: testen scheduleNextUpdate(context);
  }

	/**
	* Zentrale Methode zum Aktualisieren eines einzelnen Widgets.
	*
	* @param context   App-Kontext
	* @param mgr       AppWidgetManager
	* @param widgetId  ID des zu aktualisierenden Widgets
	*/
  private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
    Log.i("WIDGET", "⟳ updateWidget() für widgetId: " + widgetId)

    // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
    // ToDo: Testen, ob PegelLogic.run(context) hier richtig ist.
    // Evtl. in einen Hintergrunddienst oder WorkManager auslagern, falls es länger dauert.
    // PegelLogic.run(context);

    // RemoteViews-Instanz erzeugen
		val views = RemoteViews(context.getPackageName(), R.layout.widget_pegel)

		/**
		* -----------------------------------------------
		* Widget-Größe ermitteln
		* -----------------------------------------------
		*
		* Ab API 31 (Android 12) kann das System mehrere
		* mögliche Widget-Größen liefern.
		* Es wird die größte verfügbare Größe gewählt,
		* um die Grafik optimal zu rendern.
		*/
		val viewMapping: MutableMap<SizeF?, RemoteViews?>? =
        mgr.getAppWidgetOptions(widgetId).getParcelable(AppWidgetManager.OPTION_APPWIDGET_SIZES)
    var maxSize: SizeF? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewMapping != null && !viewMapping.isEmpty()) {
      // Die größte verfügbare Größe für die Bitmap-Generierung auswählen
      maxSize = viewMapping.keys.stream()
        .max(Comparator { size1: SizeF?, size2: SizeF? ->
        Float.compare(
          size1!!.getWidth() * size1.getHeight(),
          size2!!.getWidth() * size2.getHeight()
				)
			})
      .orElse(null)
    }

    var widthPx: Int
    var heightPx: Int

    if (maxSize != null) {
        widthPx = maxSize.getWidth().toInt()
        heightPx = maxSize.getHeight().toInt()
        Log.i("WIDGET", "Größe aus API 31+ (px) → " + widthPx + " x " + heightPx)
    } else {
	      // Fallback für ältere Android-Versionen
        Log.w("WIDGET", "Fallback zur alten Größenberechnung wird verwendet.")
        widthPx = getWidgetWidthInPx(context, mgr, widgetId)
        heightPx = getWidgetHeightInPx(context, mgr, widgetId)
    }
		// Sicherheits-Fallbacks
    if (widthPx <= 0) widthPx = 300
    if (heightPx <= 0) heightPx = 130

    Log.i("WIDGET", "Gerenderte Bitmapgröße (px): " + widthPx + " x " + heightPx)

		/**
		 * -----------------------------------------------
		 * Grafik erzeugen
		 * -----------------------------------------------
		 */
    val bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx)
    if (bmp != null) {
      views.setImageViewBitmap(R.id.widgetChartImage, bmp)
    } else {
	    /**
	    * -----------------------------------------------
	    * Klick auf Widget → App starten
	    * -----------------------------------------------
	    */
	    Log.e("WIDGET", "Bitmap konnte nicht erstellt werden!")
      // Optional: Ein Fehler-Icon oder Platzhalter anzeigen
      views.setImageViewResource(R.id.widgetChartImage, R.layout.widget_pegel)
    }

     // Tippen auf Widget → App öffnen
    val launchIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val openAppPendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Klick auf gesamtes Widget
    views.setOnClickPendingIntent(
        R.id.widget_root,
        openAppPendingIntent
    )

		/**
		* -----------------------------------------------
		* Pegeldaten aktualisieren & anzeigen
		* -----------------------------------------------
		*/

    //ToDo: löschen PegelLogic.run(context) // API starten und Daten abholen

    val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

    val correntValue = cache.getInt("last_value", -1)
		val correntTemp  = cache.getFloat("last_temp", 0F)
    val time: String = cache.getString("last_time", "--:--")!!

    // Übergabe letzte Wasserhöhe
    views.setTextViewText(
      R.id.widget_pegelwert,
      "Pegel: $correntValue cm"
    )

		// Übergabe letzte Wassertemperatur
		views.setTextViewText(
			R.id.widget_temp,
			"Wasser: $correntTemp °C"
		)

		//Übergabe letzte Messzeit
    views.setTextViewText(
      R.id.widget_timestamp,
      "Stand: $time Uhr"
    )

    // Das Widget-UI aktualisieren
    mgr.updateAppWidget(widgetId, views)
  }

	/**
	* Ermittelt die Widget-Breite in Pixeln
	* (Fallback für ältere Android-Versionen).
	*/
	private fun getWidgetWidthInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {
    val density = context.getResources().getDisplayMetrics().density
    // Verwenden von maxW für Landscape und minW für Portrait
    val isPortrait = context.getResources()
        .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
    val widthDp = if (isPortrait)
        mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    else
        mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
    return (widthDp * density).toInt()
  }

	/**
	* Ermittelt die Widget-Höhe in Pixeln
	* (Fallback für ältere Android-Versionen).
	*/
	private fun getWidgetHeightInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {
    val density = context.getResources().getDisplayMetrics().density
    val isPortrait = context.getResources()
        .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
    val heightDp = if (isPortrait)
        mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    else
        mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    return (heightDp * density).toInt()
  }

  private fun ToDoupdateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
	  Log.i("WIDGET", "⟳ onReceive() – action= updateWidget")

	  // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
	  //ToDo: testen PegelLogic.run(context);
	  val views = RemoteViews(context.getPackageName(), R.layout.widget_pegel)

	  // 🔥 Widget-Größe ermitteln
	  val options = mgr.getAppWidgetOptions(widgetId)

	  val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
	  val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
	  val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
	  val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

	  Log.i(
	      "WIDGET",
	      "Größe dp → minW=" + minW + " maxW=" + maxW + " minH=" + minH + " maxH=" + maxH
	  )

	  // px berechnen (dp → px)
	  val density = context.getResources().getDisplayMetrics().density
	  var widthPx = (maxW * density).toInt()
	  var heightPx = (maxH * density).toInt()

	  if (widthPx < 100) widthPx = 300 // falls System keine Daten liefert

	  if (heightPx < 100) heightPx = 130

	  Log.i("WIDGET", "Gerenderte Bitmapgröße px: " + widthPx + " x " + heightPx)


	  // Graph aus Cache erzeugen
	  val bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx)
	  views.setImageViewBitmap(R.id.widgetChartImage, bmp)

	  mgr.updateAppWidget(widgetId, views)
	  // danach Widget-UI aktualisieren
	  //ToDo: löschen WidgetUpdater.updateWidgetViews(context, mgr, appWidgetIds);
	  //ComponentName thisWidget = new ComponentName(context, PegelWidget.class);
	  //int[] ids = mgr.getAppWidgetIds(thisWidget);
	  //WidgetUpdater.updateWidgetViews(context, mgr, ids);
  }
	/**
	* Empfängt eigene Broadcasts (z.B. geplante Updates).
	*/
  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)

		when (intent.action) {

			ACTION_UPDATE_REQUEST -> {
				Log.i("WIDGET", "🔄 Daten werden geladen")
				PegelLogic.run(context)
				scheduleNextUpdate(context)
			}

			ACTION_DATA_UPDATED -> {
				Log.i("WIDGET", "🎨 Widget wird neu gerendert")
				updateAllWidgets(context)
			}
		}
  }
	private fun updateAllWidgets(context: Context) {
		val mgr = AppWidgetManager.getInstance(context)
		val ids = mgr.getAppWidgetIds(
			ComponentName(context, PegelWidget::class.java)
		)

		for (id in ids) {
			updateWidget(context, mgr, id)
		}
	}


	/**
 * Plant das nächste Widget-Update über den AlarmManager.
 * Das Intervall wird aus den App-Einstellungen gelesen.
 */
	private fun scheduleNextUpdate(context: Context) {
    val minutes = context
      .getSharedPreferences("settings", Context.MODE_PRIVATE)
      .getInt("widget_interval", 30) // 15/30/45/60

    val triggerAt = System.currentTimeMillis() + minutes * 60 * 1000

    val i = Intent(context, PegelWidget::class.java)
		i.action = ACTION_UPDATE_REQUEST

    val pi = PendingIntent.getBroadcast(
      context,
      0,
      i,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    am.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      triggerAt,
      pi
    )

    Log.i("WIDGET", "⏰ Widget-Update geplant in " + minutes + " Min")
  }

  companion object {
	  /** Eigene Action zum Aktualisieren des Widgets */
	  const val ACTION_UPDATE_REQUEST = "de.net.wiesenfarth.mainpegel.UPDATE_REQUEST"
	  const val ACTION_DATA_UPDATED  = "de.net.wiesenfarth.mainpegel.DATA_UPDATED"

  }
}