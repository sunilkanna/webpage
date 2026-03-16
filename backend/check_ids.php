<?php
include 'db_connect.php';
header('Content-Type: application/json');

$res = $conn->query("SELECT id, LENGTH(id) as len, HEX(id) as hex FROM appointments WHERE id LIKE '8%'");
$apps = [];
while ($row = $res->fetch_assoc()) {
    $apps[] = $row;
}

$count = $conn->query("SELECT COUNT(*) as cnt FROM appointments")->fetch_assoc();

echo json_encode([
    "apps" => $apps,
    "total_count" => $count['cnt']
]);
?>
