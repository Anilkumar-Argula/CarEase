package uk.ac.tees.mad.carease.data.models

data class Addon(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val isSelected: Boolean = false
) {
    companion object {
        // Default add-ons (can also fetch from Firestore)
        fun getDefaultAddons(): List<Addon> {
            return listOf(
                Addon(
                    id = "addon_1",
                    name = "Interior Vacuum",
                    description = "Complete interior vacuuming",
                    price = 5.0
                ),
                Addon(
                    id = "addon_2",
                    name = "Wax Coating",
                    description = "Premium wax protection",
                    price = 10.0
                ),
                Addon(
                    id = "addon_3",
                    name = "Brake Check",
                    description = "Full brake system inspection",
                    price = 15.0
                ),
                Addon(
                    id = "addon_4",
                    name = "Tire Rotation",
                    description = "Rotate all four tires",
                    price = 12.0
                )
            )
        }
    }
}