package uk.ac.tees.mad.carease.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class ServiceSelection(
    val serviceId: String,
    val serviceName: String,
    val serviceType: String,
    val basePrice: Double,
    val duration: Int,
    val areaId: String,
    val areaName: String,
    val areaPriceMultiplier: Double,
    val promoCode: String? = null,
    val discount: Double = 0.0
): Parcelable {
    val estimatedPrice:Double
        get()=(basePrice*areaPriceMultiplier)-discount
}

// For passing vehicle data
@Parcelize
data class VehicleData(
    val make: String,
    val model: String,
    val registrationNumber: String,
    val color: String,
    val photoUrl: String? = null,
    val notes: String = "",
    val selectedAddons: List<String> = emptyList(),
    val addonsPrice: Double = 0.0
) : Parcelable

// Foe passing complete payload to the booking screen
@Parcelize
data class BookingPayload(
    val serviceSelection: ServiceSelection,
    val vehicleData: VehicleData,
    val scheduledDate: Long? = null,
    val timeSlot: String = "",
    val totalPrice: Double = 0.0
) : Parcelable