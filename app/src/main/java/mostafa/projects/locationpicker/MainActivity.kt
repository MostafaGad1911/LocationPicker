package mostafa.projects.locationpicker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import mostafa.projects.location_picker.activities.LocationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val checkVal: Int =
            PermissionChecker.checkCallingOrSelfPermission(this, requiredPermission)
        if (checkVal == PackageManager.PERMISSION_GRANTED) {

            var locationIntent = Intent(this, LocationActivity::class.java)
            startActivityForResult(locationIntent, 150)
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