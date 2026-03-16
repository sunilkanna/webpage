<?php
include 'db_connect.php';
$conn->query("DELETE FROM appointments WHERE id = 80");
echo "Deleted 80. Affected: " . $conn->affected_rows;
$conn->close();
?>
