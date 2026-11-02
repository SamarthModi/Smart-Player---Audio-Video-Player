package com.smartplayer.app.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = Resources.getSystem().configuration.locales
            if (!locales.isEmpty) locales[0] else Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale ?: Locale.getDefault()
        }
    }

    fun applyLanguage(context: Context? = null, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context != null) {
            try {
                val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                if (languageCode == "system" || languageCode.isEmpty()) {
                    localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
                } else {
                    localeManager?.applicationLocales = LocaleList.forLanguageTags(languageCode)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (languageCode == "system" || languageCode.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun updateContextLocale(context: Context, languageCode: String): Context {
        val isSystemDefault = languageCode == "system" || languageCode.isEmpty()
        val targetLocale = if (isSystemDefault) {
            getSystemLocale()
        } else {
            Locale.forLanguageTag(languageCode)
        }

        Locale.setDefault(targetLocale)

        val res = context.resources
        val configuration = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isSystemDefault) {
                val systemLocales = Resources.getSystem().configuration.locales
                if (!systemLocales.isEmpty) {
                    configuration.setLocales(systemLocales)
                } else {
                    configuration.setLocales(LocaleList(targetLocale))
                }
            } else {
                configuration.setLocales(LocaleList(targetLocale))
            }
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = targetLocale
        }
        configuration.setLayoutDirection(targetLocale)

        @Suppress("DEPRECATION")
        res.updateConfiguration(configuration, res.displayMetrics)

        try {
            val appRes = context.applicationContext?.resources
            if (appRes != null && appRes != res) {
                val appConfig = Configuration(appRes.configuration)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (isSystemDefault) {
                        val systemLocales = Resources.getSystem().configuration.locales
                        if (!systemLocales.isEmpty) {
                            appConfig.setLocales(systemLocales)
                        } else {
                            appConfig.setLocales(LocaleList(targetLocale))
                        }
                    } else {
                        appConfig.setLocales(LocaleList(targetLocale))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    appConfig.locale = targetLocale
                }
                appConfig.setLayoutDirection(targetLocale)
                @Suppress("DEPRECATION")
                appRes.updateConfiguration(appConfig, appRes.displayMetrics)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return context.createConfigurationContext(configuration)
    }

    fun getSystemLanguageCode(): String {
        return getSystemLocale().language.lowercase()
    }

    fun getSystemLanguageDisplayName(): String {
        val systemLocale = getSystemLocale()
        val nativeName = systemLocale.getDisplayLanguage(systemLocale).replaceFirstChar { it.uppercase() }
        val englishName = systemLocale.getDisplayLanguage(Locale.ENGLISH).replaceFirstChar { it.uppercase() }

        return if (nativeName.equals(englishName, ignoreCase = true) || nativeName.isBlank()) {
            englishName.ifBlank { "English" }
        } else {
            "$nativeName ($englishName)"
        }
    }
}
