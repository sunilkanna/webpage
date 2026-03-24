<?php
include 'db_connect.php';

function test_query($conn, $label, $sql) {
    echo "TESTING: $label\n";
    $result = $conn->query($sql);
    if ($result) {
        echo "SUCCESS: $label\n";
        return true;
    } else {
        echo "FAIL: $label - " . $conn->error . "\n";
        return false;
    }
}

echo "Database Connectivity:\n";
test_query($conn, "Select Users", "SELECT 1 FROM users LIMIT 1");
test_query($conn, "Select Appointments", "SELECT 1 FROM appointments LIMIT 1");
test_query($conn, "Select Patient Profiles", "SELECT 1 FROM patient_profiles LIMIT 1");
test_query($conn, "Select Notifications", "SELECT 1 FROM notifications LIMIT 1");

echo "\nCheck Missing Columns:\n";
test_query($conn, "Check Height in Profiles", "SELECT height FROM patient_profiles LIMIT 1");
test_query($conn, "Check Session Fields in Appointments", "SELECT session_start_time, session_end_time, session_duration_minutes FROM appointments LIMIT 1");

$conn->close();
?>
