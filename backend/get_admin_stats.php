<?php
include 'db_connect.php';

// 1. Total Patients
$patient_sql = "SELECT COUNT(*) as count FROM users WHERE user_type = 'Patient'";
$patients = $conn->query($patient_sql)->fetch_assoc()['count'];

// 2. Active Counselors (Approved)
$counselor_sql = "SELECT COUNT(DISTINCT user_id) as count FROM counselor_qualifications WHERE status = 'Approved'";
$counselors = $conn->query($counselor_sql)->fetch_assoc()['count'];

// 3. Pending Verifications
$pending_sql = "SELECT COUNT(*) as count FROM counselor_qualifications WHERE status = 'Pending'";
$pending = $conn->query($pending_sql)->fetch_assoc()['count'];

// 4. System Alerts (Counting ERROR and WARNING logs in the last 24 hours)
$alerts_query = "SELECT COUNT(*) as count FROM system_logs WHERE level IN ('ERROR', 'WARNING') AND timestamp > (NOW() - INTERVAL 1 DAY)";
$alerts = (int)$conn->query($alerts_query)->fetch_assoc()['count'];

$response = [
    "status" => "success",
    "total_patients" => (int)$patients,
    "active_counselors" => (int)$counselors,
    "pending_verifications" => (int)$pending,
    "system_alerts" => $alerts
];

echo json_encode($response);

$conn->close();
?>
