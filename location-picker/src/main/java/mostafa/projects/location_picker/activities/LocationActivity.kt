package mostafa.projects.location_picker.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import mostafa.projects.location_picker.model.Address
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mostafa.projects.location_picker.GpsUtils
import java.util.*
import mostafa.projects.location_picker.R
import kotlin.math.roundToInt


class LocationActivity : androidx.appcompat.app.AppCompatActivity(), OnMapReadyCallback,
    com.google.android.gms.location.LocationListener, GpsUtils.GpsCallback {

    private var REQUEST_LOCATION_CODE = 101
    lateinit var googleMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var addressObj = Address()
    var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_location
        )
        initObjects()
        setUpMap()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 201) {
            getLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                        getLocation()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    fun setUpMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.picker_user_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }


    fun initObjects() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true
        return false
    }


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    fun checkGPSEnabled(): Boolean {
        var locEnabled = isLocationEnabled()
        if (!locEnabled)
            showAlert()
        return locEnabled
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.enable_location))
            .setMessage(getString(R.string.location_msg))
            .setPositiveButton(getString(R.string.location_settings)) { paramDialogInterface, paramInt ->
                GpsUtils(this).turnGPSOn(this)
//                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(myIntent)
            }
            .setNegativeButton(getString(R.string.location_cancel)) { paramDialogInterface, paramInt ->
                val intent = Intent()
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_CODE
                        )
                    })
                    .create()
                    .show()

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_CODE
                )
            }
        }
    }


    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                try {
                    Log.i("CURRENT_LOCATION", "${location?.latitude} , ${location?.longitude}")

                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(location?.latitude!!, location?.longitude!!)).zoom(12f)
                        .build()
                    googleMap.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(cameraPosition)
                    )

                    googleMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
                        override fun onMapClick(p0: LatLng?) {
                            googleMap?.clear()
                            if (p0 != null) {
                                val markerOptions = MarkerOptions().position(p0)
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_BLUE
                                        )
                                    )
                                googleMap!!.addMarker(markerOptions)
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val geocoder: Geocoder
                                        val addresses: List<android.location.Address>
                                        geocoder =
                                            Geocoder(this@LocationActivity, Locale.getDefault())
                                        addresses = geocoder.getFromLocation(
                                            p0.latitude,
                                            p0.longitude,
                                            1
                                        )
                                        if (addresses != null) {
                                            var title = addresses[0].getAddressLine(0)
                                            var addressValue =
                                                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                                            val city = addresses[0].locality
                                            val state = addresses[0].adminArea
                                            val country = addresses[0].countryName
                                            val postalCode = addresses[0].postalCode
                                            val knownName =
                                                addresses[0].featureName // Only if available else return NULL
                                            val streetName = addresses[0].getAddressLine(1)


                                            if (city != null) {
                                                addressObj.city = city
                                            }

                                            if (state != null) {
                                                addressObj.state = state
                                            }
                                            if (streetName != null) {
                                                addressObj.streetName = streetName
                                            }


                                            if (country != null) {
                                                addressObj.country = country
                                            }

                                            if (postalCode != null) {
                                                addressObj.postalCode = postalCode
                                            }

                                            if (knownName != null) {
                                                addressObj.knownName = knownName
                                            }

                                            var dis = distance(
                                                location?.latitude!!,
                                                location?.longitude!!,
                                                p0?.latitude!!,
                                                p0?.longitude!!
                                            )
                                            addressObj.distance = dis?.roundToInt()!!

                                            withContext(Dispatchers.Main) {
                                                markerOptions.title(title)
                                                address = title
                                            }
                                            addressObj.lat = p0.latitude
                                            addressObj.long = p0.longitude


                                            val intent = Intent()
                                            intent.putExtra("addressDetected", addressObj)
                                            setResult(Activity.RESULT_OK, intent)
                                            finish()
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                                                overridePendingTransition(
                                                    R.anim.fade_in,
                                                    R.anim.fade_out
                                                )
                                            }
                                        } else {
                                            address = ""
                                            withContext(Dispatchers.Main) {
                                                this@LocationActivity.tost(getString(R.string.detect_failed))

                                            }

                                        }
                                    } catch (e: Exception) {
                                        address = ""
                                        withContext(Dispatchers.Main) {
                                            this@LocationActivity.tost(getString(R.string.detect_failed))

                                        }
                                    }
                                }

                            }
                        }

                    })


                } catch (e: NullPointerException) {

                }
            }.addOnFailureListener {
            }

    }

    fun Context.tost(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.uiSettings?.setMyLocationButtonEnabled(true)
        googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_2))
        if (!checkGPSEnabled()) {
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            getLocation();
        }

    }

    override fun onLocationChanged(p0: Location) {
        val sydney = LatLng(p0.latitude, p0.longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onGpsStateUpdated(isGPSEnable: Boolean, locationDelay: Long) {
        if (isGPSEnable) {
            getLocation()
        } else {
            Toast.makeText(this, getString(R.string.location_msg), Toast.LENGTH_LONG).show()

        }

    }
}