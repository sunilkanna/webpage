<?php
include 'db_connect.php';
$conn->query("UPDATE appointments SET medical_report_url = 'http://test.com' WHERE id = 80");
echo "Update 80 to http://test.com. Affected: " . $conn->affected_rows . "<br>";
$res = $conn->query("SELECT medical_report_url FROM appointments WHERE id = 80");
$row = $res->fetch_assoc();
echo "SELECT shows: " . $row['medical_report_url'];
$conn->close();
?>
