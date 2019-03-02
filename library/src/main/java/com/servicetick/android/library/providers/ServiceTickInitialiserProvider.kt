package com.servicetick.android.library.providers

import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.work.Configuration
import androidx.work.WorkManager
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.di.libraryModule
import lilhermit.android.remotelogger.library.Log
import org.koin.android.ext.android.startKoin
import org.koin.log.EmptyLogger

class ServiceTickInitialiserProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.init("ServiceTickMobileSdk")

        context?.run {

            try {
                // Disable WorkManagerInitializer then initialize manually
                val workManagerInitializer = ComponentName(this, "androidx.work.impl.WorkManagerInitializer")
                packageManager.setComponentEnabledSetting(
                        workManagerInitializer,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        0
                )
                WorkManager.initialize(this, Configuration.Builder().build())
            } catch (exception: Exception) {
            }

            startKoin(this, listOf(libraryModule), logger = EmptyLogger())
            ServiceTick.internalInit(this)

        }
        return true
    }

    override fun query(
            uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int = 0

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int = 0
}