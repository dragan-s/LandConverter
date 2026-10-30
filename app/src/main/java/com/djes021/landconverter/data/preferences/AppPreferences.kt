package com.djes021.landconverter.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("land_converter_prefs", Context.MODE_PRIVATE)

    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_RUN, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_FIRST_RUN, value) }

    fun setFirstRunCompleted() {
        isFirstRun = false
    }

    private companion object {
        const val KEY_IS_FIRST_RUN = "key_is_first_run"
    }
}
