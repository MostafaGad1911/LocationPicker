package mostafa.projects.location_picker.model

import java.io.Serializable

class Address :Serializable {
    var lat:Double? = null
    var long:Double? = null
    var title:String? = null
    var city:String? = null
    var state:String? = null
    var country:String? = null
    var postalCode:String? = null
    var knownName:String? = null
    var distance:Int? = null
    var streetName:String? = null

    fun DataToString():String{
        return "Address :  { Country : ${country} , Government : ${state} , City : ${city} , StreetName ${streetName} }"
    }
}