<?php
include 'db_connect.php';

// Check if column exists
$check_col = $conn->query("SHOW COLUMNS FROM counselor_qualifications LIKE 'doctor_name'");

if ($check_col->num_rows == 0) {
    // Column doesn't exist, add it
    $sql = "ALTER TABLE counselor_qualifications ADD COLUMN doctor_name VARCHAR(100) AFTER user_id";
    if ($conn->query($sql) === TRUE) {
        echo "Successfully added 'doctor_name' column to 'counselor_qualifications' table.<br>";
    } else {
        echo "Error adding column: " . $conn->error . "<br>";
    }
} else {
    echo "Column 'doctor_name' already exists.<br>";
}

echo "Database schema check complete.";
$conn->close();
?>
