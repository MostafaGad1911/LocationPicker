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
    com.google.android.gms.location.LocationListener , GpsUtils.GpsCallback{

    private var REQUEST_LOCATION_CODE = 101
    lateinit var googleMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var addressObj = Address()
    var address = ""
    private var isLocationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_location
        )
        isLocationEnabled = checkLocationPermission(this)
        initObjects()
        setUpMap()
    }


    fun requestLocationPermission(activity: Activity, PERMISSION_REQUEST_CODE: Int) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
    fun checkLocationPermission(context: Context): Boolean {
        return !(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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


    fun Context.tost(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        if (isLocationEnabled) {
            p0?.isMyLocationEnabled = true
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener {
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it?.latitude!!, it?.longitude!!)).zoom(12f)
                    .build()
                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(cameraPosition)
                )
            }
        } else
            requestLocationPermission(this, 1024)

        googleMap.uiSettings?.setMyLocationButtonEnabled(true)
        googleMap?.uiSettings.isZoomControlsEnabled = true
        googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_2))
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


                                if (addresses[0].locality != null) {
                                    addressObj.city = addresses[0].locality
                                }

                                if (addresses[0].adminArea != null) {
                                    addressObj.state = addresses[0].adminArea
                                }
                                if (addresses[0].getAddressLine(1) != null) {
                                    addressObj.streetName = addresses[0].getAddressLine(1)
                                }


                                if (addresses[0].countryName != null) {
                                    addressObj.country = addresses[0].countryName
                                }

                                if (addresses[0].postalCode != null) {
                                    addressObj.postalCode = addresses[0].postalCode
                                }

                                if (addresses[0].featureName != null) {
                                    addressObj.knownName = addresses[0].featureName
                                }


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
                            Log.i("GeoCoderFailed" , "${e.message}")
                            address = ""
                            withContext(Dispatchers.Main) {
                                this@LocationActivity.tost(getString(R.string.detect_failed))

                            }
                        }
                    }

                }
            }

        })


    }

    @RequiresApi(Build.VERSION_CODES.DONUT)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isLocationEnabled = false
        when (requestCode) {
            1024 -> // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnabled = true

                    if (isGPSEnabled(this))
                        onGpsStateUpdated(true, 1000)
                    else
                        GpsUtils(this).turnGPSOn(this)
                } else requestLocationPermission(this, 1024)
        }
    }

    fun isGPSEnabled(activity: Activity): Boolean {
        val locationManager: LocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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

    }

}