package de.rki.coronawarnapp.covidcertificate.common.statecheck

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccStateChecker @Inject constructor(
    private val timeStamper: TimeStamper,
    private val appConfigProvider: AppConfigProvider,
    private val dscRepository: DscRepository,
    private val dscSignatureValidator: DscSignatureValidator,
    private val expirationChecker: DccExpirationChecker,
) {

    suspend fun checkState(
        dccData: DccData<*>?
    ): Flow<CwaCovidCertificate.State> = combine(
        appConfigProvider.currentConfig,
        dscRepository.dscData
    ) { appConfig, dscData ->
        if (dccData == null) {
            Timber.tag(TAG).w("Certificate was null.")
            return@combine CwaCovidCertificate.State.Invalid()
        }
        try {
            dscSignatureValidator.validateSignature(dccData = dccData, preFetchedDscData = dscData)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Certificate had invalid signature.")
            return@combine CwaCovidCertificate.State.Invalid()
        }

        val nowUtc = timeStamper.nowUTC

        return@combine expirationChecker.getExpirationState(
            dccData = dccData,
            expirationThreshold = appConfig.covidCertificateParameters.expirationThreshold,
            now = nowUtc
        )
    }

    companion object {
        private const val TAG = "DccStateChecker"
    }
}
