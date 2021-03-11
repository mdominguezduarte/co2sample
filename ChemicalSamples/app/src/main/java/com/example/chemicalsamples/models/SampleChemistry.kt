package com.example.chemicalsamples.models

import java.io.Serializable

data class SampleChemistry(
    val id: Int,
    val title: String,
    val image: String,
    val sample_1: String,
    val sample_2: String,
    val sample_3: String,
    val sample_time_1: String,
    val sample_time_2: String,
    val sample_time_3: String,
    val valueResult: String,
    val valueExplanation: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
): Serializable