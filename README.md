- Few lines of code to detect your address , lat , long from map , just intent to location activity with result and click pick location button affter detecting your location on map to start geocoding process to get your address

    var locationIntent = Intent(this, LocationActivity::class.java)
    
    startActivityForResult(locationIntent, 2021)

you can pass your current location to map with this parameters

     locationIntent.putExtra("latitude" , userLatLng.latitude)
     locationIntent.putExtra("longitude" , userLatLng.longitude)

and recieve it in onactivity results ( City , Country , State , Postal Code , Address ) // Note that one of these params can be null

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


 
 <img src="https://user-images.githubusercontent.com/25991597/110681788-e042d380-81e2-11eb-98f5-105bac8b2230.jpg" width="400" height="800" />

 <img src="https://user-images.githubusercontent.com/25991597/110532061-09049380-8125-11eb-983c-1830f290ddae.jpg" width="400" height="800" />

 <img src="https://user-images.githubusercontent.com/25991597/110699130-3883d080-81f7-11eb-9d22-b5395aa54cb6.jpg" width="400" height="800" />
 
 



	
	
	   
