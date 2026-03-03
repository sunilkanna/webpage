<?php
include 'db_connect.php';

$patient_id = $_GET['patient_id'] ?? null;

if (!$patient_id) {
    echo json_encode(["status" => "error", "message" => "Patient ID required"]);
    exit();
}

// Fetch latest risk assessment
$risk_stmt = $conn->prepare("SELECT risk_score, risk_category, assessed_at FROM risk_assessments WHERE patient_id = ? ORDER BY assessed_at DESC LIMIT 1");
$risk_stmt->bind_param("i", $patient_id);
$risk_stmt->execute();
$risk_result = $risk_stmt->get_result();
$risk_assessment = $risk_result->fetch_assoc();
$risk_stmt->close();

// Fetch patient reports
$reports_stmt = $conn->prepare("SELECT file_name as title, 'Uploaded' as status, uploaded_at as date, 'Patient uploaded report' as description FROM patient_reports WHERE patient_id = ? ORDER BY uploaded_at DESC");
$reports_stmt->bind_param("i", $patient_id);
$reports_stmt->execute();
$reports_result = $reports_stmt->get_result();

$reports = [];
while ($row = $reports_result->fetch_assoc()) {
    $reports[] = $row;
}
$reports_stmt->close();

// Format response for Android App
$response = [
    "status" => "success",
    "risk_assessment" => $risk_assessment ?: [
        "risk_score" => 0,
        "risk_category" => "Not Assessed",
        "assessed_at" => "Never"
    ],
    "reports" => $reports
];

echo json_encode($response);

$conn->close();
?>
