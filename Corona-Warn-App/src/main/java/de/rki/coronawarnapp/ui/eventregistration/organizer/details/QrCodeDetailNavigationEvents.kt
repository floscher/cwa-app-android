package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationCategory

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToQrCodePosterFragment(val locationId: Long) : QrCodeDetailNavigationEvents()
    data class NavigateToDuplicateFragment(val traceLocation: TraceLocation, val category: TraceLocationCategory) :
        QrCodeDetailNavigationEvents()
}
