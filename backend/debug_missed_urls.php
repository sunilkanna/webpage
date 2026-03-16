<?php
include 'db_connect.php';
header('Content-Type: application/json');

$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url LIKE '%/uploads/%'");
$remaining = [];
while ($row = $res->fetch_assoc()) {
    $remaining[] = $row;
}

echo json_encode([
    "count" => count($remaining),
    "examples" => array_slice($remaining, 0, 10)
]);
?>
