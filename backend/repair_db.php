<?php
include 'db_connect.php';

echo "Checking database schema...<br>";

// 1. Check if 'doctor_name' column exists in 'counselor_qualifications'
$table = "counselor_qualifications";
$column = "doctor_name";

$check_sql = "SHOW COLUMNS FROM `$table` LIKE '$column'";
$result = $conn->query($check_sql);

if ($result && $result->num_rows == 0) {
    echo "Column '$column' does not exist in table '$table'. Adding it now...<br>";
    $alter_sql = "ALTER TABLE `$table` ADD COLUMN `$column` VARCHAR(100) AFTER `user_id`";
    if ($conn->query($alter_sql) === TRUE) {
        echo "Successfully added column '$column'.<br>";
    } else {
        echo "Error adding column: " . $conn->error . "<br>";
    }
} else {
    echo "Column '$column' already exists in table '$table'.<br>";
}

echo "Database verification complete.";
$conn->close();
?>
