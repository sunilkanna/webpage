<?php
include 'db_connect.php';

// Add rejection_reason column to appointments table if it doesn't exist
$result = $conn->query("SHOW COLUMNS FROM appointments LIKE 'rejection_reason'");
if ($result->num_rows === 0) {
    $conn->query("ALTER TABLE appointments ADD COLUMN rejection_reason VARCHAR(500) DEFAULT NULL");
    echo json_encode(["status" => "success", "message" => "Added rejection_reason column to appointments table"]);
} else {
    echo json_encode(["status" => "success", "message" => "rejection_reason column already exists"]);
}

$conn->close();
?>
