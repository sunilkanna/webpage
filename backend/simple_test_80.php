<?php
include 'db_connect.php';
$conn->query("UPDATE appointments SET medical_report_url = 'test' WHERE id = 80");
echo "Update 80 to 'test'. Affected: " . $conn->affected_rows;
$conn->close();
?>
