package app.bdw.in_appupdate

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private var appUpdateManager: AppUpdateManager? = null

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Update was successful
            Toast.makeText(this, "Update completed successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Update was cancelled or failed
            Toast.makeText(this, "Update failed or cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private val updateResultStarter =
        IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()
            // launch updateLauncher
            updateLauncher.launch(request)
        }

    // Create a listener to track request state updates.
    val listener = InstallStateUpdatedListener { state ->
        // (Optional) Provide a download progress bar.
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            // Show update progress bar.
        }
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Toast.makeText(this, "New app is ready", Toast.LENGTH_SHORT).show()
        }
        // Log state or install the update.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            appUpdateManager = AppUpdateManagerFactory.create(this)

            checkUpdate()
            appUpdateManager?.unregisterListener(listener)

            Greeting(
                name = "Android"
            )

        }
    }

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            // This example applies an flexible update. To apply a immediate update
            // instead, pass in AppUpdateType.IMMEDIATE
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Request the update
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        updateResultStarter,
                        //pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        // Include a request code to later monitor this update request.
                        101
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Something wrong went wrong!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("CheckUpdate", "No Update available")
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}