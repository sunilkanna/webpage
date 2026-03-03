package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class GetCounselorReportsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("reports") val reports: List<CounselorReportItem>
)

data class CounselorReportItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String, 
    @SerializedName("category") val category: String = "General", 
    @SerializedName("date") val date: String,
    @SerializedName("patientName") val patientName: String,
    @SerializedName("patientId") val patientId: Int,
    @SerializedName("fileUrl") val fileUrl: String
)
