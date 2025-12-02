package uk.ac.tees.mad.carease.data.models

data class Service(
    val id: String = "",
    val name: String = "",
    val type: String = "", // "WASH", "SERVICE", "BOTH"
    val description: String = "",
    val price: Double = 0.0,
    val duration: Int = 0, // in minutes
    val isActive: Boolean = true
)