package com.example.data

import com.example.data.local.CouponEntity

sealed class CouponValidationResult {
    data class Success(
        val coupon: CouponEntity,
        val discountAmount: Double,
        val finalPrice: Double,
        val description: String
    ) : CouponValidationResult()

    data class Error(val message: String) : CouponValidationResult()
}
