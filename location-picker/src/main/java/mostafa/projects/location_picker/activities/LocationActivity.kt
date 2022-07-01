package mostafa.projects.location_picker.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import mostafa.projects.location_picker.R
import mostafa.projects.location_picker.model.Address
import java.util.*


class LocationActivity : androidx.appcompat.app.AppCompatActivity(), OnMapReadyCallback,
    com.google.android.gms.location.LocationListener, GpsUtils.GpsCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var addressObj = Address()
    private var address = ""
    private var isLocationEnabled: Boolean = false
    private var prefHelper: SharedPreferences? = null
    private lateinit var submitLocationBtn: Button
    private var currentLatLng: LatLng? = null
    private lateinit var dialog: Dialog

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_location
        )
        prefHelper = applicationContext.getSharedPreferences(
            "location_helper",
            MODE_PRIVATE
        )
        initViews()
        initObjects()
        setUpMap()
        getLocationPermission()
        checkLocationResult()
    }

    override fun onResume() {
        getLocationPermission()
        super.onResume()
    }
    private fun checkLocationResult() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    getLocationPermission()

                }
            }

    }

    private fun initViews() {
        submitLocationBtn = findViewById(R.id.submitLocationBtn)
        submitLocationBtn.setBackgroundResource(R.drawable.grey_border)
        submitLocationBtn.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray))

        submitLocationBtn.setOnClickListener {
            if (currentLatLng != null) {
                currentLatLng?.geoCodeLatLng()
            }
        }
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //Can ask user for permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    906
                )
            } else {
                val goSettingsTxt = dialog.findViewById<TextView>(R.id.goSettingsTxt)
                val closeImg = dialog.findViewById<ImageView>(R.id.closeImg)
                closeImg.setOnClickListener {
                    dialog.dismiss()
                    val intent = Intent()
                    setResult(Activity.RESULT_CANCELED, intent)
                    finish()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                }
                goSettingsTxt.setOnClickListener {
                    val intent = Intent()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    }
                    val uri = Uri.fromParts(
                        "package", this@LocationActivity.packageName,
                        null
                    )
                    intent.data = uri
                    dialog.dismiss()
                    resultLauncher.launch(intent)
                }
                dialog.setOnCancelListener {
                    val intent = Intent()
                    setResult(Activity.RESULT_CANCELED, intent)
                    finish()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                }
                dialog.show()
            }
        } else {
            isLocationEnabled = true
            setUpMap()
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun Dialog.initDialog(lyt: Int): Dialog {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        this.setCancelable(true)
        this.setCanceledOnTouchOutside(true)
        this.setContentView(lyt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            this.window?.setWindowAnimations(
                R.style.DialogFragmentAnimation
            )
        }
        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )


        return this
    }


    private fun setUpMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.picker_user_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }


    private fun initObjects() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dialog = Dialog(
            this,
            R.style.DialogFragmentAnimation
        ).initDialog(R.layout.location_warn)
        dialog.setCancelable(false)

    }


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
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
        p0.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.latitude, it.longitude)).zoom(17f)
                    .build()
                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(cameraPosition)
                )
            }
        }


        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        googleMap.uiSettings?.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_map))
        googleMap.setOnMapClickListener { p1 ->
            googleMap.clear()
            if (p1 != null) {
                currentLatLng = p1
                submitLocationBtn.setBackgroundResource(R.drawable.sky_border)
                submitLocationBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sky))
                val markerOptions = MarkerOptions().position(p1)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
                googleMap.addMarker(markerOptions)
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(p1.latitude, p1.longitude)).zoom(17f)
                    .build()
                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(cameraPosition)
                )

            }
        }


    }

    private fun LatLng.geoCodeLatLng() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses: List<android.location.Address>
                val geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
                addresses = geocoder.getFromLocation(
                    this@geoCodeLatLng.latitude,
                    this@geoCodeLatLng.longitude,
                    1
                )
                if (addresses != null && addresses.isNotEmpty()) {
                    val title = addresses[0].getAddressLine(0)


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
                        address = title
                    }
                    addressObj.lat = this@geoCodeLatLng.latitude
                    addressObj.long = this@geoCodeLatLng.longitude


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
                    val intent = Intent()
                    addressObj.lat = this@geoCodeLatLng.latitude
                    addressObj.long = this@geoCodeLatLng.longitude
                    addressObj.country =
                        "${this@geoCodeLatLng.latitude} - ${this@geoCodeLatLng.longitude}"
                    intent.putExtra("addressDetected", addressObj)
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }
            } catch (e: Exception) {
                Log.i("GeoCoderFailed", "${e.message}")
                address = ""
                withContext(Dispatchers.Main) {
                    val intent = Intent()
                    addressObj.country =
                        "${this@geoCodeLatLng.latitude} - ${this@geoCodeLatLng.longitude}"
                    addressObj.lat = this@geoCodeLatLng.latitude
                    addressObj.long = this@geoCodeLatLng.longitude
                    intent.putExtra("addressDetected", addressObj)
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.DONUT)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUpMap()
            isLocationEnabled = true
            googleMap.uiSettings?.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.style_map_2
                )
            )
            googleMap.setOnMapClickListener { p0 ->
                googleMap.clear()
                if (p0 != null) {
                    val markerOptions = MarkerOptions().position(p0)
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE
                            )
                        )
                    googleMap.addMarker(markerOptions)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val addresses: List<android.location.Address>
                            val geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
                            addresses = geocoder.getFromLocation(
                                p0.latitude,
                                p0.longitude,
                                1
                            )
                            if (addresses != null && addresses.isNotEmpty()) {
                                val title = addresses[0].getAddressLine(0)


                                if (addresses[0].locality != null) {
                                    addressObj.city = addresses[0].locality
                                }

                                if (addresses[0].adminArea != null) {
                                    addressObj.state = addresses[0].adminArea
                                }
                                if (addresses[0].getAddressLine(1) != null) {
                                    addressObj.streetName =
                                        addresses[0].getAddressLine(1)
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
                                    val intent = Intent()
                                    addressObj.country = "${p0.latitude} - ${p0.longitude}"
                                    addressObj.lat = p0.latitude
                                    addressObj.long = p0.longitude
                                    intent.putExtra("addressDetected", addressObj)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()

                                }

                            }
                        } catch (e: Exception) {
                            Log.i("GeoCoderFailed", "${e.message}")
                            address = ""
                            withContext(Dispatchers.Main) {
                                val intent = Intent()
                                addressObj.country = "${p0.latitude} - ${p0.longitude}"
                                addressObj.lat = p0.latitude
                                addressObj.long = p0.longitude
                                intent.putExtra("addressDetected", addressObj)
                                setResult(Activity.RESULT_OK, intent)
                                finish()

                            }
                        }
                    }

                }
            }
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
                if (it != null) {
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(it.latitude, it.longitude)).zoom(12f)
                        .build()
                    googleMap.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(cameraPosition)
                    )
                }
            }


        } else {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

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

    }

}