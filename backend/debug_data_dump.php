<?php
include 'db_connect.php';
echo "USERS:\n";
$res = $conn->query('SELECT id, full_name, user_type FROM users');
while($row = $res->fetch_assoc()) echo json_encode($row) . "\n";
echo "\nAPPOINTMENTS:\n";
$res = $conn->query('SELECT id, patient_id, counselor_id, status FROM appointments');
while($row = $res->fetch_assoc()) echo json_encode($row) . "\n";
?>
