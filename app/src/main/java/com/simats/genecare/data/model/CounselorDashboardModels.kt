package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class GetCounselorAppointmentsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("appointments") val appointments: List<CounselorAppointmentData>
)

data class CounselorAppointmentData(
    @SerializedName("id") val id: Int,
    @SerializedName("appointment_date") val date: String,
    @SerializedName("time_slot") val time: String,
    @SerializedName("status") val status: String,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("reason") val reason: String?,
    @SerializedName("image_initial") val imageInitial: String?,
    @SerializedName("image_color_hex") val imageColorHex: String?,
    @SerializedName("has_report") val hasReport: Boolean?,
    @SerializedName("report_url") val reportUrl: String?
)



data class GetCounselorPatientsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("patients") val patients: List<CounselorPatientData>
)

data class CounselorPatientData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?, // age might be null if DOB not set
    @SerializedName("gender") val gender: String?,
    @SerializedName("date") val lastVisit: String,
    @SerializedName("condition_name") val condition: String?,
    @SerializedName("status") val status: String
)
