<?php
include 'db_connect.php';
$conn->query("CREATE TABLE IF NOT EXISTS write_test (id INT PRIMARY KEY, val VARCHAR(10))");
if ($conn->query("INSERT INTO write_test (id, val) VALUES (1, 'test') ON DUPLICATE KEY UPDATE val = 'test2'")) {
    echo "Write successful. Affected: " . $conn->affected_rows;
} else {
    echo "Write failed: " . $conn->error;
}
$conn->close();
?>
