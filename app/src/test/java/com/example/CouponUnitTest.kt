package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CouponValidationResult
import com.example.data.MahfazaRepository
import com.example.data.local.CouponEntity
import com.example.model.EducationLevel
import com.example.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CouponUnitTest {

    private lateinit var context: Context
    private lateinit var repository: MahfazaRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = MahfazaRepository(context)
        runBlocking {
            repository.seedDemoData()
            val now = System.currentTimeMillis()
            val future = now + (365L * 86_400_000L)
            repository.addCoupon(
                CouponEntity(
                    code = "SERKAN20",
                    discountType = "PERCENTAGE",
                    discountValue = 20.0,
                    startDate = now - 86_400_000L,
                    endDate = future,
                    totalUsageLimit = 500,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true
                )
            )
            repository.addCoupon(
                CouponEntity(
                    code = "HOSGELDIN100",
                    discountType = "FIXED_TRY",
                    discountValue = 100.0,
                    startDate = now - 86_400_000L,
                    endDate = future,
                    totalUsageLimit = 200,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true
                )
            )
            repository.addCoupon(
                CouponEntity(
                    code = "YILLIK30",
                    discountType = "PERCENTAGE",
                    discountValue = 30.0,
                    startDate = now - 86_400_000L,
                    endDate = future,
                    totalUsageLimit = 100,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true,
                    applicableBillingPeriod = "YEARLY"
                )
            )
        }
    }

    @Test
    fun `test valid percentage discount coupon SERKAN20`() = runBlocking {
        val result = repository.validateCoupon(
            code = "SERKAN20",
            userId = "u_perc_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1000.0
        )

        assertTrue(result is CouponValidationResult.Success)
        val success = result as CouponValidationResult.Success
        assertEquals(200.0, success.discountAmount, 0.01)
        assertEquals(800.0, success.finalPrice, 0.01)
        assertEquals("SERKAN20", success.coupon.code)
    }

    @Test
    fun `test valid fixed TRY discount coupon HOSGELDIN100`() = runBlocking {
        val result = repository.validateCoupon(
            code = "HOSGELDIN100",
            userId = "u_fixed_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1200.0
        )

        assertTrue(result is CouponValidationResult.Success)
        val success = result as CouponValidationResult.Success
        assertEquals(100.0, success.discountAmount, 0.01)
        assertEquals(1100.0, success.finalPrice, 0.01)
    }

    @Test
    fun `test case insensitive coupon code input`() = runBlocking {
        val result = repository.validateCoupon(
            code = "serkan20", // lowercase input
            userId = "u_case_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 500.0
        )

        assertTrue(result is CouponValidationResult.Success)
        val success = result as CouponValidationResult.Success
        assertEquals(100.0, success.discountAmount, 0.01)
    }

    @Test
    fun `test non existent coupon code returns error`() = runBlocking {
        val result = repository.validateCoupon(
            code = "INVALID_CODE_999",
            userId = "u_err_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1000.0
        )

        assertTrue(result is CouponValidationResult.Error)
        val error = result as CouponValidationResult.Error
        assertTrue(error.message.contains("bulunamayan") || error.message.contains("Geçersiz"))
    }

    @Test
    fun `test passive coupon code returns error`() = runBlocking {
        val now = System.currentTimeMillis()
        val passiveCoupon = CouponEntity(
            code = "PASIF50",
            discountType = "PERCENTAGE",
            discountValue = 50.0,
            startDate = now - 86400000L,
            endDate = now + 86400000L,
            totalUsageLimit = 10,
            perUserLimit = 1,
            usedCount = 0,
            isActive = false
        )
        repository.addCoupon(passiveCoupon)

        val result = repository.validateCoupon(
            code = "PASIF50",
            userId = "u_pasif_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1000.0
        )

        assertTrue(result is CouponValidationResult.Error)
        val error = result as CouponValidationResult.Error
        assertTrue(error.message.contains("pasif"))
    }

    @Test
    fun `test expired coupon code returns error`() = runBlocking {
        val now = System.currentTimeMillis()
        val expiredCoupon = CouponEntity(
            code = "EXPIRED10",
            discountType = "PERCENTAGE",
            discountValue = 10.0,
            startDate = now - 10000000L,
            endDate = now - 100000L, // in the past
            totalUsageLimit = 10,
            perUserLimit = 1,
            usedCount = 0,
            isActive = true
        )
        repository.addCoupon(expiredCoupon)

        val result = repository.validateCoupon(
            code = "EXPIRED10",
            userId = "u_exp_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1000.0
        )

        assertTrue(result is CouponValidationResult.Error)
        val error = result as CouponValidationResult.Error
        assertTrue(error.message.contains("süresi dolmuştur"))
    }

    @Test
    fun `test coupon total usage limit reached returns error`() = runBlocking {
        val now = System.currentTimeMillis()
        val limitCoupon = CouponEntity(
            code = "DOLUKOD",
            discountType = "FIXED_TRY",
            discountValue = 50.0,
            startDate = now - 86400000L,
            endDate = now + 86400000L,
            totalUsageLimit = 5,
            perUserLimit = 1,
            usedCount = 5, // fully used
            isActive = true
        )
        repository.addCoupon(limitCoupon)

        val result = repository.validateCoupon(
            code = "DOLUKOD",
            userId = "u_lim_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1000.0
        )

        assertTrue(result is CouponValidationResult.Error)
        val error = result as CouponValidationResult.Error
        assertTrue(error.message.contains("limiti dolmuştur"))
    }

    @Test
    fun `test billing period restricted coupon YILLIK30`() = runBlocking {
        // Valid for YEARLY
        val validResult = repository.validateCoupon(
            code = "YILLIK30",
            userId = "u_yearly_1",
            packageId = "std_pro",
            billingPeriod = "YEARLY",
            originalPrice = 2400.0
        )
        assertTrue(validResult is CouponValidationResult.Success)

        // Invalid for MONTHLY
        val invalidResult = repository.validateCoupon(
            code = "YILLIK30",
            userId = "u_yearly_2",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1200.0
        )
        assertTrue(invalidResult is CouponValidationResult.Error)
    }

    @Test
    fun `test discount capping at original price`() = runBlocking {
        val now = System.currentTimeMillis()
        val hugeCoupon = CouponEntity(
            code = "DEV2000",
            discountType = "FIXED_TRY",
            discountValue = 2000.0, // Exceeds original price
            startDate = now - 86400000L,
            endDate = now + 86400000L,
            totalUsageLimit = 10,
            perUserLimit = 1,
            usedCount = 0,
            isActive = true
        )
        repository.addCoupon(hugeCoupon)

        val result = repository.validateCoupon(
            code = "DEV2000",
            userId = "u_cap_1",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 500.0
        )

        assertTrue(result is CouponValidationResult.Success)
        val success = result as CouponValidationResult.Success
        assertEquals(500.0, success.discountAmount, 0.01) // Capped
        assertEquals(0.0, success.finalPrice, 0.01) // Does not go negative
    }

    @Test
    fun `test process test payment updates user subscription and increments coupon usage`() = runBlocking {
        val initialCoupon = repository.getCouponByCode("SERKAN20")
        assertNotNull(initialCoupon)
        val initialUsedCount = initialCoupon!!.usedCount

        val success = repository.processTestPayment(
            userId = "s1",
            packageId = "std_pro",
            billingPeriod = "YEARLY",
            appliedCouponCode = "SERKAN20",
            originalPrice = 2400.0
        )

        assertTrue(success)

        val updatedUser = repository.getUserById("s1")
        assertNotNull(updatedUser)
        assertEquals("std_pro", updatedUser!!.packageId)
        assertEquals("PAID_YEARLY", updatedUser.subscriptionPlan)

        val updatedCoupon = repository.getCouponByCode("SERKAN20")
        assertNotNull(updatedCoupon)
        assertEquals(initialUsedCount + 1, updatedCoupon!!.usedCount)
    }

    @Test
    fun `test per user usage limit enforced after payment`() = runBlocking {
        // First payment
        val pay1 = repository.processTestPayment(
            userId = "s2",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            appliedCouponCode = "HOSGELDIN100",
            originalPrice = 1200.0
        )
        assertTrue(pay1)

        // Attempting to reuse same coupon with same user s2
        val secondValidation = repository.validateCoupon(
            code = "HOSGELDIN100",
            userId = "s2",
            packageId = "std_pro",
            billingPeriod = "MONTHLY",
            originalPrice = 1200.0
        )

        assertTrue(secondValidation is CouponValidationResult.Error)
        val error = secondValidation as CouponValidationResult.Error
        assertTrue(error.message.contains("daha önce kullandınız"))
    }
}
