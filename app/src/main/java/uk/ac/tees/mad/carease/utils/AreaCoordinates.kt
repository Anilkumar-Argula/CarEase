package uk.ac.tees.mad.carease.utils

object AreaCoordinates {
    // Map area IDs to coordinates
    private val coordinates = mapOf(
        "area_1" to Pair(53.4808, -2.2426),  // Manchester
        "area_2" to Pair(51.5074, -0.1278),  // London
        "area_3" to Pair(52.4862, -1.8904),  // Birmingham
        "area_4" to Pair(53.4084, -2.9916)   // Liverpool
    )

    fun getCoordinates(areaId: String): Pair<Double, Double>? {
        return coordinates[areaId]
    }
}