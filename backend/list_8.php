<?php
include 'db_connect.php';
header('Content-Type: application/json');

$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE id LIKE '8%'");
$apps = [];
while ($row = $res->fetch_assoc()) {
    $apps[] = $row;
}
echo json_encode($apps);
?>
