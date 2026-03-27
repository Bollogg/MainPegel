package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import de.net.wiesenfarth.mainpegel.databinding.FragmentSecondBinding

/*******************************************************
 * Klasse:     SecondFragment
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Dieses Fragment stellt die zweite Ansicht innerhalb
 * der Navigation Component dar.
 *
 * Es dient als Ziel-Screen vom FirstFragment und
 * demonstriert die Rücknavigation innerhalb der App.
 *
 * Funktionen:
 * -----------------------------------------------------
 * • Verwendung von ViewBinding für sicheren UI-Zugriff
 * • Navigation zurück zum FirstFragment
 * • Lifecycle-sicheres Ressourcenmanagement
 *
 * Navigation:
 * -----------------------------------------------------
 * FirstFragment → SecondFragment → FirstFragment
 *
 * Architektur:
 * -----------------------------------------------------
 * Das Fragment folgt dem empfohlenen Lifecycle-Muster:
 *
 * onCreateView  → Initialisierung des Bindings
 * onViewCreated → UI-Logik & Listener
 * onDestroyView → Freigabe des Bindings
 *
 * Wichtig:
 * -----------------------------------------------------
 * Das Binding darf nur zwischen onCreateView() und
 * onDestroyView() verwendet werden!
 *
 * @Autor:     Bollogg
 * @Datum:     2026-03-27
 *******************************************************/
class SecondFragment : Fragment() {

	/**
	 * Nullable ViewBinding Referenz (Lifecycle-abhängig)
	 */
	private var _binding: FragmentSecondBinding? = null

	/**
	 * Nicht-null Zugriff auf das Binding
	 *
	 * Achtung:
	 * ------------------------------------------------
	 * Nur gültig zwischen onCreateView() und onDestroyView()
	 */
	private val binding get() = _binding!!

	/**
	 * Erstellt die View des Fragments.
	 *
	 * Aufgabe:
	 * ------------------------------------------------
	 * • Layout über ViewBinding inflaten
	 * • Root-View zurückgeben
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentSecondBinding.inflate(inflater, container, false)
		return binding.root
	}

	/**
	 * Wird nach Erstellung der View aufgerufen.
	 *
	 * Aufgabe:
	 * ------------------------------------------------
	 * • Setzen von UI-Listenern
	 * • Initialisierung von Benutzerinteraktionen
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// Button → Navigation zurück zum FirstFragment
		binding.buttonSecond.setOnClickListener {
			findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
		}
	}

	/**
	 * Lifecycle-Methode zum Freigeben der View-Ressourcen.
	 *
	 * Wichtig:
	 * ------------------------------------------------
	 * • Binding muss auf null gesetzt werden
	 * • Verhindert Memory Leaks
	 */
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}