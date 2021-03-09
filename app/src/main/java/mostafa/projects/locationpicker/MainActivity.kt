package mostafa.projects.locationpicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import mostafa.projects.location_picker.activities.LocationActivity
import mostafa.projects.location_picker.model.Address

class MainActivity : AppCompatActivity() , View.OnClickListener {

    lateinit var pick_loc_btn:Button
    lateinit var city_name_txt:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()


    }

    private fun initViews() {
        city_name_txt = findViewById(R.id.city_name_txt)
        pick_loc_btn = findViewById(R.id.pick_loc_btn)

        pick_loc_btn.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 2021) {
            var address = data?.getSerializableExtra("addressDetected") as Address
            city_name_txt.setText("${address.title} - ${address.lat} - ${address.long}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        var locationIntent = Intent(this, LocationActivity::class.java)
                        startActivityForResult(locationIntent, 150)
                        overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )

                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.pick_loc_btn -> {
                val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION
                val checkVal: Int =
                    PermissionChecker.checkCallingOrSelfPermission(this, requiredPermission)
                if (checkVal == PackageManager.PERMISSION_GRANTED) {

                    var locationIntent = Intent(this, LocationActivity::class.java)
                    startActivityForResult(locationIntent, 2021)
                    overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )



                } else {
                    ActivityCompat.requestPermissions(
                        this!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 120
                    )
                }
            }
        }
    }
}