<?php
include 'db_connect.php';

$columns = [
    'session_start_time' => "TIMESTAMP NULL",
    'session_end_time' => "TIMESTAMP NULL",
    'session_duration_minutes' => "INT DEFAULT 0",
    'medical_report_url' => "VARCHAR(255) NULL"
];

foreach ($columns as $column => $definition) {
    $check = $conn->query("SHOW COLUMNS FROM appointments LIKE '$column'");
    if ($check->num_rows == 0) {
        $sql = "ALTER TABLE appointments ADD COLUMN $column $definition";
        if ($conn->query($sql) === TRUE) {
            echo "Successfully added column '$column' to 'appointments' table.<br>";
        } else {
            echo "Error adding column '$column': " . $conn->error . "<br>";
        }
    } else {
        echo "Column '$column' already exists.<br>";
    }
}

echo "Appointments table update complete.";
$conn->close();
?>
