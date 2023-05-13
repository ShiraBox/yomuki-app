package me.urfate.yomuki.ui.item

import android.content.Context
import androidx.preference.SwitchPreferenceCompat
import me.urfate.yomuki.R

class MaterialSwitchPreference(context: Context): SwitchPreferenceCompat(context) {
    init {
        widgetLayoutResource = R.layout.item_material_switch_preference
    }
}
