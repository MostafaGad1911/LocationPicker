- Few lines of code to detect your address , lat , long from map , just intent for activity with result and click pick location button affter detecting your location on map to start geocoding process to get your address

                    var locationIntent = Intent(this, LocationActivity::class.java)
                    startActivityForResult(locationIntent, 2021)

and recieve it in onactivity results 

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 2021) {
            var address = data?.getSerializableExtra("addressDetected") as Address
            city_name_txt.setText("${address.title} - ${address.lat} - ${address.long}")
        }
    }


# LocationPicker

  implementation 'com.github.MostafaGad1911:SharedSettings:Tag'

   
# Step 1
          Add it in your root build.gradle at the end of repositories:
          
         - allprojects {
		
	         repositories {
		
			  maven { url 'https://jitpack.io' }
		       } 
	       }

# Step 2 : Add the dependency
 

	   implementation 'com.github.MostafaGad1911:LocationPicker:Tag'
