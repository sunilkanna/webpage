<?php
include 'db_connect.php';

$queries = [
    "ALTER TABLE appointments ADD COLUMN IF NOT EXISTS medical_report_url VARCHAR(255) NULL AFTER meeting_link",
    "ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reason VARCHAR(255) NULL AFTER medical_report_url",
    "ALTER TABLE appointments ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(50) DEFAULT 'Video Call' AFTER reason"
];

foreach ($queries as $sql) {
    if ($conn->query($sql)) {
        echo "Successfully executed: $sql\n";
    } else {
        echo "Error executing $sql: " . $conn->error . "\n";
    }
}

$conn->close();
?>
