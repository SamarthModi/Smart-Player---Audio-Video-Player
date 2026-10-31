package com.smartplayer.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(languageCode: String) {
        if (languageCode == "system") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun updateContextLocale(context: Context, languageCode: String): Context {
        if (languageCode == "system") return context

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        return context.createConfigurationContext(configuration)
    }
}
