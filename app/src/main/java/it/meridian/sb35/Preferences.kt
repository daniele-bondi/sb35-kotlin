package it.meridian.sb35

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

class Preferences : PreferenceFragmentCompat()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true) // NOTE: Hack to hide the options menu from previous fragment
	}
	
	/**
	 * Called during [.onCreate] to supply the preferences for this fragment.
	 * Subclasses are expected to call [.setPreferenceScreen] either
	 * directly or via helper methods such as [.addPreferencesFromResource].
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 * a previous saved state, this is the state.
	 * @param rootKey If non-null, this preference fragment should be rooted at the
	 * [android.support.v7.preference.PreferenceScreen] with this key.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		this.addPreferencesFromResource(R.xml.preferences)
	}
}
