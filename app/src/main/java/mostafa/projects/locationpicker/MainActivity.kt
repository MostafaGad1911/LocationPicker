package mostafa.projects.locationpicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import mostafa.projects.location_picker.activities.LocationActivity
import mostafa.projects.location_picker.model.Address

class MainActivity : AppCompatActivity() , View.OnClickListener {

    lateinit var pick_loc_btn:Button

    lateinit var city_name_txt:TextView
    lateinit var state_name_txt:TextView
    lateinit var country_name_txt:TextView
    lateinit var postalCode_name_txt:TextView
    lateinit var knownName_name_txt:TextView
    lateinit var latlong_name_txt:TextView

    lateinit var userLatLng:LatLng
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObjects()
        initViews()

    }

    fun initObjects(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!)
    }
    fun checkPermissions() {
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val checkVal: Int =
            PermissionChecker.checkCallingOrSelfPermission(this, requiredPermission)
        if (checkVal == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    try {
                        userLatLng = LatLng(location!!.latitude, location!!.longitude)
                        var locationIntent = Intent(this, LocationActivity::class.java)
                        if(userLatLng != null){
                            locationIntent.putExtra("latitude" , userLatLng.latitude)
                            locationIntent.putExtra("longitude" , userLatLng.longitude)
                        }
//                        showToast("${userLatLng.latitude},${userLatLng.longitude}")

                        startActivityForResult(locationIntent, 2021)
                        overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )

                    } catch (e: NullPointerException) {

                    }
                }.addOnFailureListener {
                    Log.w("LocationException", it!!.message!!)
                }



        } else {
            ActivityCompat.requestPermissions(
                this!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 120
            )
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    private fun initViews() {
        city_name_txt = findViewById(R.id.city_name_txt)
        state_name_txt = findViewById(R.id.state_name_txt)
        country_name_txt = findViewById(R.id.country_name_txt)
        postalCode_name_txt = findViewById(R.id.postalCode_name_txt)
        knownName_name_txt = findViewById(R.id.knownName_name_txt)
        latlong_name_txt = findViewById(R.id.latlong_name_txt)
        pick_loc_btn = findViewById(R.id.pick_loc_btn)
        pick_loc_btn.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 2021) {
            var address = data?.getSerializableExtra("addressDetected") as Address
            address.city?.let {
                city_name_txt.setText("City = ${address.city}")
            }
            address.state?.let {
                state_name_txt.setText("State = ${address.state}")
            }
            address.country?.let {
                country_name_txt.setText("Country = ${address.country}")
            }
            address.postalCode?.let {
                postalCode_name_txt.setText("Postal Code = ${address.postalCode}")
            }
            address.knownName?.let {
                knownName_name_txt.setText("Known name = ${address.knownName}")
            }
            address.lat?.let {
                latlong_name_txt.setText("LatLong = ${address.lat} , ${address.long}")
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            120 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        mFusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                try {
                                    userLatLng = LatLng(location!!.latitude, location!!.longitude)
                                    var locationIntent = Intent(this, LocationActivity::class.java)
                                    if(userLatLng != null){
                                        locationIntent.putExtra("latitude" , userLatLng.latitude)
                                        locationIntent.putExtra("longitude" , userLatLng.longitude)
                                    }
//                                    showToast("${userLatLng.latitude},${userLatLng.longitude}")

                                    startActivityForResult(locationIntent, 2021)
                                    overridePendingTransition(
                                        R.anim.fade_in,
                                        R.anim.fade_out
                                    )

                                } catch (e: NullPointerException) {

                                }
                            }.addOnFailureListener {
                                Log.w("LocationException", it!!.message!!)
                            }

                    }
                } else {
                    Toast.makeText(this, "يا اسطي اقبل متهزرش معايا", Toast.LENGTH_SHORT).show()


                }
                return
            }
        }
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.pick_loc_btn -> {

                var locationIntent = Intent(this, LocationActivity::class.java)

                startActivityForResult(locationIntent, 2021)


            }
        }
    }
}