package com.hfad.soundsavev1

data class AdviceSlipResponse(
    val slip: Slip
)

data class Slip(
    val id: Int,
    val advice: String
)