<?php
include 'db_connect.php';
header('Content-Type: application/json');

$app82 = $conn->query("SELECT patient_id FROM appointments WHERE id = 82")->fetch_assoc();
$patient_id = $app82['patient_id'];

$reports = [];
$res = $conn->query("SELECT * FROM patient_reports WHERE patient_id = $patient_id");
if ($res) {
    while ($row = $res->fetch_assoc()) {
        $reports[] = $row;
    }
}

echo json_encode([
    "patient_id" => $patient_id,
    "reports" => $reports
]);
?>
