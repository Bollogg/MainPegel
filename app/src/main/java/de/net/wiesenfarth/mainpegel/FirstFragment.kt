package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import de.net.wiesenfarth.mainpegel.databinding.FragmentFirstBinding

/*******************************************************
 * Klasse:     FirstFragment
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Dieses Fragment stellt einen einfachen Einstiegsscreen
 * innerhalb der Navigation Component dar.
 *
 * Es dient als Beispiel bzw. Platzhalter für die erste
 * Ansicht der App und demonstriert die Navigation zu
 * einem zweiten Fragment.
 *
 * Funktionen:
 * -----------------------------------------------------
 * • Verwendung von ViewBinding zur sicheren UI-Zugriff
 * • Navigation über NavController
 * • Lifecycle-sicheres Handling von Views
 *
 * Navigation:
 * -----------------------------------------------------
 * FirstFragment → SecondFragment
 * (über Navigation Component / nav_graph)
 *
 * Architektur:
 * -----------------------------------------------------
 * Fragment nutzt ViewBinding und ist vollständig
 * lifecycle-aware implementiert:
 *
 * onCreateView  → Binding initialisieren
 * onDestroyView → Binding freigeben
 *
 * Wichtig:
 * -----------------------------------------------------
 * Das Binding darf nur zwischen onCreateView() und
 * onDestroyView() verwendet werden!
 *
 * @Autor:     Bollogg
 * @Datum:     2026-03-27
 *******************************************************/
class FirstFragment : Fragment() {

	/**
	 * ViewBinding Referenz (nullable wegen Lifecycle)
	 */
	private var _binding: FragmentFirstBinding? = null

	/**
	 * Nicht-null Zugriff auf Binding
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
	 * • Inflate des Layouts über ViewBinding
	 * • Rückgabe der Root-View
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentFirstBinding.inflate(inflater, container, false)
		return binding.root
	}

	/**
	 * Wird aufgerufen, nachdem die View erstellt wurde.
	 *
	 * Aufgabe:
	 * ------------------------------------------------
	 * • Setzen von Click-Listenern
	 * • Initialisierung von UI-Logik
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// Button → Navigation zum SecondFragment
		binding.buttonFirst.setOnClickListener {
			findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
		}
	}

	/**
	 * Lifecycle-Methode zum Aufräumen der View.
	 *
	 * Wichtig:
	 * ------------------------------------------------
	 * • Binding MUSS hier auf null gesetzt werden
	 * • Verhindert Memory Leaks
	 */
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}