# Location Picker 
[![](https://jitpack.io/v/MostafaGad1911/LocationPicker.svg)](https://jitpack.io/#MostafaGad1911/LocationPicker)

 With a few lines of code you can to **detect your address, lat, long** from map, just intent to location activity with result and click pick location button affter detecting your location on map to start geocoding process to get your address.
 
 Usage
To use the LocationPickerActivity first you need to add these lines to your AndroidManifest file:

``` xml 
        <activity
            android:name="mostafa.projects.location_picker.activities.LocationActivity"
            android:theme="@style/Theme.MaterialComponents.Light.NoActionBar"
            android:windowSoftInputMode="adjustPan"
            >
        </activity>
```

``` kotlin 
     var locationIntent = Intent(this, LocationActivity::class.java) 
     startActivityForResult(locationIntent, 2021)
```
then override **onActivityResult** abd get your data  ( City, Country, State, Postal Code, Address )

**NOTE**:: May  one of these params can be null. 
 ``` kotlin  
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
``` 


# Getting Started 
## Step 1: Add it to build.gradle (project level) at the end of repositories:

 ``` kotlin  
             allprojects 
               {
	         repositories 
		       {	
			  maven { url 'https://jitpack.io' }
		       } 
	       }
```          
        

## Step 2 : Add the dependency  [![](https://jitpack.io/v/MostafaGad1911/LocationPicker.svg)](https://jitpack.io/#MostafaGad1911/LocationPicker)	   

 ``` kotlin  
        implementation 'com.github.MostafaGad1911:LocationPicker:1.1.1'
        
```        

# Sample 


 
<div>
 <img src="https://user-images.githubusercontent.com/25991597/176960261-52bf4744-f4a5-42a8-89cc-f0f4c04bcbf6.jpg"  width="50%" height="800"   />
<img width="20"/>
<img width="20"/>
 <img src="https://user-images.githubusercontent.com/25991597/110699130-3883d080-81f7-11eb-9d22-b5395aa54cb6.jpg" width="50%" height="800"/>
<div/>
 
 



	
	
	   
