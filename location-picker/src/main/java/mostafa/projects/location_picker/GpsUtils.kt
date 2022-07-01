package mostafa.projects.location_picker

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class GpsUtils(private val context: Context) {

    private val mLocationSettingsRequest: LocationSettingsRequest

    private val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(context)
    private val locationManager: LocationManager = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationRequest: LocationRequest = LocationRequest.create()

    init {
        locationRequest.priority = LocationRequest.PRIORITY_NO_POWER
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
                .Builder().addLocationRequest(locationRequest)

        mLocationSettingsRequest = builder.build()

        builder.setAlwaysShow(true) //this is the key ingredient
    }


    // method for turn on GPS
    @RequiresApi(Build.VERSION_CODES.DONUT)
    fun turnGPSOn(gpsCallback: GpsCallback?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsCallback?.onGpsStateUpdated(true)
        } else {
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener((context as Activity)) {
                        //  GPS is already enable, callback GPS status through listener
                        gpsCallback?.onGpsStateUpdated(true)
                    }
                    .addOnFailureListener(context) { e ->
                        when ((e as ApiException).statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(context, 201)
                            } catch (sie: SendIntentException) {
                                Log.e("GpsUtils", "PendingIntent unable to execute request.")
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                val errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                Log.e("GpsUtils", errorMessage)
                            }

                        }
                    }

        }
    }

    interface GpsCallback {
        /**
         * Call this method if the GPS status is updated any time.
         * @param isGPSEnable Boolean that specify the GPS current status.
         * @param locationDelay time in millisecond before request the current location.
         */
        fun onGpsStateUpdated(isGPSEnable: Boolean, locationDelay: Long = 3000)
    }
}