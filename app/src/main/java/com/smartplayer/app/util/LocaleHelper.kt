package com.smartplayer.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(languageCode: String) {
        if (languageCode == "system" || languageCode.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun updateContextLocale(context: Context, languageCode: String): Context {
        if (languageCode == "system" || languageCode.isEmpty()) return context

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val res = context.resources
        val configuration = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        @Suppress("DEPRECATION")
        res.updateConfiguration(configuration, res.displayMetrics)

        return context.createConfigurationContext(configuration)
    }
}
