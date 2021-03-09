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
	   
	
	
# Sample 


 <img src="https://user-images.githubusercontent.com/25991597/110532051-06a23980-8125-11eb-86b5-c2090fe2b537.jpg" width="400" height="800" />
 
 <img src="https://user-images.githubusercontent.com/25991597/110532060-086bfd00-8125-11eb-8b31-02a842449ca7.jpg" width="400" height="800" />

 <img src="https://user-images.githubusercontent.com/25991597/110532061-09049380-8125-11eb-983c-1830f290ddae.jpg" width="400" height="800" />

 <img src="https://user-images.githubusercontent.com/25991597/110532068-099d2a00-8125-11eb-8122-6a5a29f8967f.jpg" width="400" height="800" />


	
	
	   
