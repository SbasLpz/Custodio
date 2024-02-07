package com.miapp.custodio2.Utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.miapp.custodio2.BuildConfig
import com.miapp.custodio2.R

class Actualizaciones {

    var appUpdateManager: AppUpdateManager? =  null

    fun updateApp(act: Activity){
        appUpdateManager = AppUpdateManagerFactory.create(act)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update.

                appUpdateManager!!.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // an activity result launcher registered via registerForActivityResult
                    AppUpdateType.FLEXIBLE,
                    // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                    // flexible updates.
                    act, 2025)
            }
        }

        // Before starting an update, register a listener for updates.
        //appUpdateManager!!.registerListener(listener)

    }



}