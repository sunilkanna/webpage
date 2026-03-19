package com.simats.genecare.data.model

data class GetPatientResultsResponse(
    val status: String,
    val risk_assessment: RiskAssessmentResult?,
    val reports: List<PatientReport>
)

data class RiskAssessmentResult(
    val risk_score: Int,
    val risk_category: String,
    val assessed_at: String
)

data class PatientReport(
    val title: String,
    val status: String,
    val date: String,
    val description: String,
    val url: String?,
    val file_url: String? = null,
    val type: String? = "report" // "report" or "assessment"
)
