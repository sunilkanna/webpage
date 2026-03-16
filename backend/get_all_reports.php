<?php
include 'db_connect.php';

// 1. Get Patient Reports
$patient_sql = "SELECT pr.*, u.full_name as patient_name FROM patient_reports pr JOIN users u ON pr.patient_id = u.id ORDER BY pr.uploaded_at DESC";
$p_result = $conn->query($patient_sql);
$patient_reports = [];
while($row = $p_result->fetch_assoc()) {
    $row['type'] = 'Patient Upload';
    $patient_reports[] = $row;
}

// 2. Get Counselor Reports
$counselor_sql = "SELECT cr.*, u.full_name as counselor_name FROM counselor_reports cr JOIN users u ON cr.counselor_id = u.id ORDER BY cr.created_at DESC";
$c_result = $conn->query($counselor_sql);
$counselor_reports = [];
while($row = $c_result->fetch_assoc()) {
    $row['type'] = 'Counselor Generated';
    $counselor_reports[] = $row;
}

echo json_encode([
    "status" => "success", 
    "reports" => array_merge($patient_reports, $counselor_reports)
]);

$conn->close();
?>
