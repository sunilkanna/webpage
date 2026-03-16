<?php
include 'db_connect.php';
header('Content-Type: application/json');

$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url LIKE 'content://%'");
$content_uris = [];
while ($row = $res->fetch_assoc()) {
    $content_uris[] = $row;
}

$id82 = $conn->query("SELECT id, medical_report_url FROM appointments WHERE id = 82")->fetch_assoc();

echo json_encode([
    "total_content_uris" => count($content_uris),
    "examples" => array_slice($content_uris, 0, 10),
    "appointment_82" => $id82
]);
?>
