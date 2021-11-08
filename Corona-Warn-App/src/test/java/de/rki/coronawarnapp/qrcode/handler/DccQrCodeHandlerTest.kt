package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.BlocklistValidator
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccQrCodeHandlerTest : BaseTest() {

    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var recoverCertificateRepository: RecoveryCertificateRepository
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator
    @MockK lateinit var blocklistValidator: BlocklistValidator

    @MockK lateinit var testCertificateContainer: TestCertificateContainer
    @MockK lateinit var recoveryCertificateContainer: RecoveryCertificateContainer
    @MockK lateinit var vaccinationCertificate: VaccinationCertificate

    @MockK lateinit var testCertID: TestCertificateContainerId
    @MockK lateinit var recoveryCertID: RecoveryCertificateContainerId
    @MockK lateinit var vaccinationCertID: VaccinationCertificateContainerId

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { blocklistValidator.isValid(any(), any()) } returns true
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } just Runs

        coEvery { testCertificateRepository.registerCertificate(any()) } returns testCertificateContainer
            .apply { every { containerId } returns testCertID }
        coEvery { vaccinationRepository.registerCertificate(any()) } returns vaccinationCertificate
            .apply { every { containerId } returns vaccinationCertID }
        coEvery { recoverCertificateRepository.registerCertificate(any()) } returns recoveryCertificateContainer
            .apply { every { containerId } returns recoveryCertID }
    }

    // TODO: more tests

    @Test
    fun `handleQrCode calls Vax repo`() = runBlockingTest {
        val dccQrCode = mockk<VaccinationCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().handleQrCode(dccQrCode, emptyList()) shouldBe vaccinationCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            vaccinationRepository.registerCertificate(any())
        }
    }

    @Test
    fun `handleQrCode calls Test repo`() = runBlockingTest {
        val dccQrCode = mockk<TestCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().handleQrCode(dccQrCode, emptyList()) shouldBe testCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            testCertificateRepository.registerCertificate(any())
        }
    }

    @Test
    fun `handleQrCode calls Recovery repo`() = runBlockingTest {
        val dccQrCode = mockk<RecoveryCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().handleQrCode(dccQrCode, emptyList()) shouldBe recoveryCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            recoverCertificateRepository.registerCertificate(any())
        }
    }

    private fun handler() = DccQrCodeHandler(
        testCertificateRepository = testCertificateRepository,
        vaccinationRepository = vaccinationRepository,
        recoveryCertificateRepository = recoverCertificateRepository,
        dscSignatureValidator = dscSignatureValidator,
        blocklistValidator = blocklistValidator
    )
}
