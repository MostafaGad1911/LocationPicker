package mostafa.projects.location_picker.model

import java.io.Serializable

class Address : Serializable {
    var lat: Double? = null
    var long: Double? = null
    var title: String? = null
    var city: String? = null
    var state: String? = null
    var country: String? = null
    var postalCode: String? = null
    var knownName: String? = null
    var distance: Int? = null
    var streetName: String? = null

    fun DataToString(): String {
        return "Address :  { ${
            this.country?.NotNullOrEmpty()?.let {  "Country : ${country} ," }
        } ${state?.NotNullOrEmpty()?.let {  "Government : ${state} ," }} ${
            city?.NotNullOrEmpty()?.let {  "City : ${city} ," }
        } ${knownName?.NotNullOrEmpty()?.let {  "Street Name : ${knownName} ," }} }"
    }
    
    fun String.NotNullOrEmpty():Boolean{
        if(this.isEmpty() || this == null)
            return false
        return true
    }

}