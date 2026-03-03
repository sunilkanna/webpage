<?php
include 'db_connect.php';

echo "<h2>Database Schema Check</h2>";

$sql = "SHOW COLUMNS FROM appointments LIKE 'medical_report_url'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    echo "<p style='color: green;'>Column <b>medical_report_url</b> exists in <b>appointments</b> table.</p>";
} else {
    echo "<p style='color: red;'>Column <b>medical_report_url</b> is MISSING in <b>appointments</b> table!</p>";
    echo "<p>Running migration to add the column...</p>";
    
    $alter = "ALTER TABLE appointments ADD COLUMN medical_report_url VARCHAR(255) AFTER meeting_link";
    if ($conn->query($alter)) {
        echo "<p style='color: green;'>Column added successfully.</p>";
    } else {
        echo "<p style='color: red;'>Failed to add column: " . $conn->error . "</p>";
    }
}

$conn->close();
?>
