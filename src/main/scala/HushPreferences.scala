package me.lessis

import android.preference.PreferenceActivity
import android.os.Bundle

class HushPreferences extends PreferenceActivity {
  override def onCreate(prev: Bundle) {
    super.onCreate(prev)
    addPreferencesFromResource(R.xml.preferences)
    /*findPreference("quiet_places") match {
      case p: Preference =>
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          def onPreferenceClick(pc: Preference) = {
            true
          }
        })
    }*/
  }
}
