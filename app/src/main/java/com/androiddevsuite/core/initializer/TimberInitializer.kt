/**
 * Android Development Suite - Timber Initializer
 * منصة تطوير أندرويد الشاملة
 * 
 * Startup initializer for Timber logging
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.core.initializer

import android.content.Context
import androidx.startup.Initializer
import com.androiddevsuite.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
