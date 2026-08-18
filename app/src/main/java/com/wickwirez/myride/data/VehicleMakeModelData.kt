package com.wickwirez.myride.data

val CAR_MAKES: List<String> = listOf(
    "Acura", "Alfa Romeo", "Aston Martin", "Audi", "Bentley", "BMW", "Buick",
    "Cadillac", "Chevrolet", "Chrysler", "Dodge", "Ferrari", "Fiat", "Ford",
    "Genesis", "GMC", "Honda", "Hyundai", "Infiniti", "Jaguar", "Jeep", "Kia",
    "Lamborghini", "Land Rover", "Lexus", "Lincoln", "Maserati", "Mazda",
    "McLaren", "Mercedes-Benz", "Mini", "Mitsubishi", "Nissan", "Polestar",
    "Porsche", "Ram", "Rivian", "Rolls-Royce", "Subaru", "Tesla", "Toyota",
    "Volkswagen", "Volvo"
)

val MODELS_BY_MAKE: Map<String, List<String>> = mapOf(
    "Acura" to listOf("ILX", "Integra", "MDX", "RDX", "TLX", "ZDX"),
    "Audi" to listOf("A3", "A4", "A5", "A6", "A7", "A8", "Q3", "Q5", "Q7", "Q8", "e-tron", "RS 6", "S4"),
    "BMW" to listOf("2 Series", "3 Series", "4 Series", "5 Series", "7 Series", "X1", "X3", "X5", "X7", "i4", "iX", "M3", "M5"),
    "Buick" to listOf("Enclave", "Encore", "Encore GX", "Envision", "Envista"),
    "Cadillac" to listOf("CT4", "CT5", "Escalade", "XT4", "XT5", "XT6", "Lyriq"),
    "Chevrolet" to listOf("Blazer", "Bolt EV", "Camaro", "Colorado", "Corvette", "Equinox", "Malibu", "Silverado 1500", "Silverado 2500HD", "Suburban", "Tahoe", "Trailblazer", "Traverse", "Trax"),
    "Chrysler" to listOf("300", "Pacifica"),
    "Dodge" to listOf("Challenger", "Charger", "Durango", "Hornet"),
    "Ford" to listOf("Bronco", "Bronco Sport", "Edge", "Escape", "Expedition", "Explorer", "F-150", "F-250", "F-350", "Maverick", "Mustang", "Mustang Mach-E", "Ranger", "Transit"),
    "GMC" to listOf("Acadia", "Canyon", "Sierra 1500", "Sierra 2500HD", "Terrain", "Yukon"),
    "Genesis" to listOf("G70", "G80", "G90", "GV60", "GV70", "GV80"),
    "Honda" to listOf("Accord", "Civic", "CR-V", "HR-V", "Odyssey", "Passport", "Pilot", "Ridgeline"),
    "Hyundai" to listOf("Elantra", "Ioniq 5", "Ioniq 6", "Kona", "Palisade", "Santa Fe", "Sonata", "Tucson", "Venue"),
    "Infiniti" to listOf("Q50", "Q60", "QX50", "QX55", "QX60", "QX80"),
    "Jaguar" to listOf("E-Pace", "F-Pace", "F-Type", "I-Pace", "XF"),
    "Jeep" to listOf("Cherokee", "Compass", "Gladiator", "Grand Cherokee", "Renegade", "Wagoneer", "Wrangler"),
    "Kia" to listOf("Carnival", "EV6", "EV9", "Forte", "K5", "Niro", "Seltos", "Sorento", "Soul", "Sportage", "Telluride"),
    "Land Rover" to listOf("Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Evoque", "Range Rover Sport"),
    "Lexus" to listOf("ES", "GX", "IS", "LX", "NX", "RX", "TX", "UX"),
    "Lincoln" to listOf("Aviator", "Corsair", "Nautilus", "Navigator"),
    "Mazda" to listOf("CX-30", "CX-5", "CX-50", "CX-90", "Mazda3", "MX-5 Miata"),
    "Mercedes-Benz" to listOf("A-Class", "C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE", "GLS", "EQE", "EQS"),
    "Mini" to listOf("Clubman", "Cooper", "Countryman"),
    "Mitsubishi" to listOf("Eclipse Cross", "Mirage", "Outlander", "Outlander Sport"),
    "Nissan" to listOf("Altima", "Ariya", "Frontier", "Kicks", "Maxima", "Murano", "Pathfinder", "Rogue", "Sentra", "Titan", "Versa", "Z"),
    "Polestar" to listOf("Polestar 2", "Polestar 3", "Polestar 4"),
    "Porsche" to listOf("718 Cayman", "911", "Cayenne", "Macan", "Panamera", "Taycan"),
    "Ram" to listOf("1500", "2500", "3500", "ProMaster"),
    "Rivian" to listOf("R1S", "R1T"),
    "Subaru" to listOf("Ascent", "Crosstrek", "Forester", "Impreza", "Legacy", "Outback", "Solterra", "WRX"),
    "Tesla" to listOf("Model 3", "Model S", "Model X", "Model Y", "Cybertruck"),
    "Toyota" to listOf("4Runner", "Camry", "Corolla", "Crown", "Highlander", "Land Cruiser", "Prius", "RAV4", "Sequoia", "Sienna", "Tacoma", "Tundra"),
    "Volkswagen" to listOf("Atlas", "Golf", "ID.4", "Jetta", "Taos", "Tiguan"),
    "Volvo" to listOf("S60", "S90", "XC40", "XC60", "XC90", "EX30", "EX90")
)

val VEHICLE_YEARS: List<Int> = (java.time.Year.now().value + 1 downTo 1980).toList()

val FUEL_TYPES: List<String> = listOf("Regular", "Mid-Grade", "Premium", "Diesel", "E85", "Electric")
