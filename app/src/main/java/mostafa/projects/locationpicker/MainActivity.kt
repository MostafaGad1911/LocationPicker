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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
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
        if (resultCode == Activity.RESULT_OK && requestCode == 203) {
            var address = data?.getSerializableExtra("addressDetected") as Address
            Log.i("Address" , "${address.DataToString()}")
            city_name_txt.setText(address.city)
            state_name_txt.setText(address.state)
            country_name_txt.setText(address.country)
            postalCode_name_txt.setText(address.postalCode)
            knownName_name_txt.setText(address.knownName)
            latlong_name_txt.setText("${address.lat},${address.long}")


        }
    }



    override fun onClick(v: View?) {
        when(v?.id){
            R.id.pick_loc_btn -> {

                val intent = Intent(this, LocationActivity::class.java)
                startActivityForResult(intent , 203)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)


            }
        }
    }
}