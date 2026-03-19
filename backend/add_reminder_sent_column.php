<?php
include 'db_connect.php';

// Add reminder_sent column to appointments table if it doesn't exist
$result = $conn->query("SHOW COLUMNS FROM appointments LIKE 'reminder_sent'");
if ($result->num_rows === 0) {
    $conn->query("ALTER TABLE appointments ADD COLUMN reminder_sent TINYINT DEFAULT 0");
    echo json_encode(["status" => "success", "message" => "Added reminder_sent column to appointments table"]);
} else {
    echo json_encode(["status" => "success", "message" => "reminder_sent column already exists"]);
}

$conn->close();
?>
