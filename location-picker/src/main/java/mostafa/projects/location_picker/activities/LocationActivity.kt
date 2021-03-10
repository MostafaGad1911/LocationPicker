package mostafa.projects.location_picker.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import mostafa.projects.location_picker.PermissionUtils
import mostafa.projects.location_picker.R
import mostafa.projects.location_picker.model.Address
import java.util.*

class LocationActivity : AppCompatActivity(), OnMapReadyCallback , View.OnClickListener{

    lateinit var location_address_map: SupportMapFragment

    var countryLatitude: Double? = 0.0
    var countryLongitude: Double? = 0.0

    lateinit var newUserLatLng: LatLng
    private var enableLocationPermissionRequest = true

    var address = ""
    lateinit var confirm_loc_address_btn:Button
    var addressObj = Address()

    var userCurrentLoc:LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        confirm_loc_address_btn = findViewById(R.id.confirm_loc_address_btn)
        location_address_map =
            (supportFragmentManager?.findFragmentById(R.id.detector_map) as? SupportMapFragment)!!
        location_address_map.getMapAsync(this)


        confirm_loc_address_btn.setOnClickListener(this)
        intent.let {
            countryLatitude = it.getDoubleExtra("latitude", 0.0)
            countryLongitude = it.getDoubleExtra("longitude", 0.0)
            userCurrentLoc = LatLng(countryLatitude!! , countryLongitude!!)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.detector_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermission() {
        if (enableLocationPermissionRequest &&
            PermissionUtils.shouldRequestLocationStoragePermission(applicationContext)
        ) {
            PermissionUtils.requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("LocationReady" , "launched")

        googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_2))
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
        googleMap.isMyLocationEnabled = true
        Log.i("isMyLocationEnabled" , "launched")

        if (userCurrentLoc != null) {
            val cameraPosition =
                CameraPosition.Builder().target(LatLng(userCurrentLoc?.latitude!!, userCurrentLoc?.longitude!!)).zoom(16.0f)
                    .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.moveCamera(cameraUpdate)
        }

        googleMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                googleMap?.clear()
                if (p0 != null) {
                    newUserLatLng = p0
                    val markerOptions = MarkerOptions().position(newUserLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    googleMap!!.addMarker(markerOptions)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val geocoder: Geocoder
                            val addresses: List<android.location.Address>
                            geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
                            addresses = geocoder.getFromLocation(
                                newUserLatLng.latitude,
                                newUserLatLng.longitude,
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
                                val knownName = addresses[0].featureName // Only if available else return NULL


                                if(city != null){
                                    addressObj.city = city
                                }

                                if(state != null){
                                    addressObj.state = state
                                }

                                if(country != null){
                                    addressObj.country = country
                                }

                                if(postalCode != null){
                                    addressObj.postalCode = postalCode
                                }

                                if(knownName != null){
                                    addressObj.knownName = knownName
                                }


                                withContext(Dispatchers.Main) {
                                    markerOptions.title(title)
                                    address = title
                                }
                            } else {
                                address = ""
                                withContext(Dispatchers.Main){
                                    Toast.makeText(this@LocationActivity, "Cannot detect your address please try again", Toast.LENGTH_LONG).show()

                                }

                            }
                        } catch (e: Exception) {
                            address = ""
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@LocationActivity, "Cannot detect your address please try again", Toast.LENGTH_LONG).show()

                            }
                        }
                    }

                }
            }

        })

    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.confirm_loc_address_btn -> {

                if (!::newUserLatLng.isInitialized) {
                    showToast(msg = getString(R.string.add_address))
                } else if (TextUtils.isEmpty(address)) {
                    showToast(getString(R.string.add_address))
                } else {
                    addressObj.lat = newUserLatLng.latitude
                    addressObj.long = newUserLatLng.longitude

                    showToast(address)

                    val intent = Intent()
                    intent.putExtra("addressDetected", addressObj)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

                }
            }
        }
    }
}
