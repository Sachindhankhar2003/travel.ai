package com.citygo.app.domain.model

object CityData {
    val cities = listOf(
        // Delhi NCR
        City("New Delhi", "Delhi",           LatLng(28.6139, 77.2090), isPopular = true),
        City("Noida", "Uttar Pradesh",       LatLng(28.5355, 77.3910), isPopular = true),
        City("Greater Noida", "Uttar Pradesh", LatLng(28.4745, 77.5040), isPopular = true),
        City("Ghaziabad", "Uttar Pradesh",   LatLng(28.6692, 77.4538), isPopular = true),
        City("Gurugram", "Haryana",          LatLng(28.4595, 77.0266), isPopular = true),
        City("Faridabad", "Haryana",         LatLng(28.4089, 77.3178), isPopular = true),
        // Haryana
        City("Chandigarh", "Chandigarh",     LatLng(30.7333, 76.7794), isPopular = true),
        City("Ambala", "Haryana",            LatLng(30.3782, 76.7767)),
        City("Panchkula", "Haryana",         LatLng(30.6942, 76.8606)),
        City("Rohtak", "Haryana",            LatLng(28.8955, 76.6066)),
        City("Panipat", "Haryana",           LatLng(29.3909, 76.9635)),
        City("Karnal", "Haryana",            LatLng(29.6857, 76.9905)),
        City("Hisar", "Haryana",             LatLng(29.1492, 75.7217)),
        City("Sonipat", "Haryana",           LatLng(28.9931, 77.0151)),
        City("Bhiwani", "Haryana",           LatLng(28.7975, 76.1322)),
        City("Rewari", "Haryana",            LatLng(28.1997, 76.6165)),
        City("Jhajjar", "Haryana",           LatLng(28.6082, 76.6565)),
        City("Manesar", "Haryana",           LatLng(28.3686, 76.9340)),
        City("Palwal", "Haryana",            LatLng(28.1428, 77.3295)),
        City("Sirsa", "Haryana",             LatLng(29.5339, 75.0206)),
        City("Bahadurgarh", "Haryana",       LatLng(28.6814, 76.9285)),
        City("Ballabhgarh", "Haryana",       LatLng(28.3459, 77.3228)),
        // Uttar Pradesh
        City("Meerut", "Uttar Pradesh",      LatLng(28.9845, 77.7064), isPopular = true),
        City("Agra","Uttar Pradesh",         LatLng(27.1767,78.0081), isPopular = true),
        City("Lucknow", "Uttar Pradesh",     LatLng(26.8467, 80.9462), isPopular = true),
        City("Bulandshahr","Uttar Pradesh",  LatLng(28.4072,77.8497)),
        City("Hapur", "Uttar Pradesh",       LatLng(28.7290, 77.7757)),
        City("Muradnagar","Uttar Pradesh",   LatLng(28.7797,77.4960)),
        City("Pilkhuwa","Uttar Pradesh",     LatLng(28.7165,77.6456)),
        City("Modinagar","Uttar Pradesh",    LatLng(28.8319,77.5757)),
        City("Bagpat","Uttar Pradesh",       LatLng(28.9446,77.2161)),
        City("Mathura","Uttar Pradesh",      LatLng(27.4924,77.6737)),
        City("Aligarh","Uttar Pradesh",      LatLng(27.8974,78.0880)),
        // Rajasthan
        City("Jaipur", "Rajasthan",          LatLng(26.9124, 75.7873), isPopular = true),
        City("Jodhpur","Rajasthan",          LatLng(26.2389,73.0243), isPopular = true),
        City("Udaipur","Rajasthan",          LatLng(24.5854,73.7125), isPopular = true),
        City("Kota","Rajasthan",             LatLng(25.2138,75.8648)),
        City("Ajmer","Rajasthan",            LatLng(26.4499,74.6399)),
        City("Bikaner","Rajasthan",          LatLng(28.0229,73.3119)),
        City("Alwar","Rajasthan",            LatLng(27.5530,76.6346)),
        City("Bhiwadi","Rajasthan",          LatLng(28.1983,76.8515)),
        City("Neemrana","Rajasthan",         LatLng(27.9741,76.3878)),
        City("Dausa","Rajasthan",            LatLng(26.8853,76.3353)),
        City("Bharatpur","Rajasthan",        LatLng(27.2152,77.4870)),
        City("Kishangarh","Rajasthan",       LatLng(26.5994,74.8607)),
        City("Sikar","Rajasthan",            LatLng(27.6094,75.1398)),
        City("Pali","Rajasthan",             LatLng(25.7711,73.3234)),
    )

    val popularCities get() = cities.filter { it.isPopular }
}
