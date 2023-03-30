package com.example.localfinedustinformation.data.models.tmcoordinates


import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("x")
    val x: Double?,
    @SerializedName("y")
    val y: Double?
)