<?php
include 'db_connect.php';
header('Content-Type: application/json');

$reports = [];
$res = $conn->query("SELECT id, file_url FROM patient_reports LIMIT 10");
if ($res) {
    while ($row = $res->fetch_assoc()) {
        $reports[] = $row;
    }
}

$appointments = [];
$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url IS NOT NULL LIMIT 10");
if ($res) {
    while ($row = $res->fetch_assoc()) {
        $appointments[] = $row;
    }
}

echo json_encode([
    "patient_reports" => $reports,
    "appointments" => $appointments
]);
?>
