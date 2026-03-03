<?php
include 'db_connect.php';

echo "--- Direct Backend Chat Verification ---\n\n";

$patientId = 1004;
$counselorId = 39;
$randomUserId = 2;

// 1. Verify Appointment Existence Logic
echo "Checking appointment between 1004 and 39...\n";
$check_sql = "SELECT id FROM appointments 
              WHERE ((patient_id = ? AND counselor_id = ?) OR (patient_id = ? AND counselor_id = ?)) 
              AND status IN ('Confirmed', 'Pending', 'Completed')";
$stmt = $conn->prepare($check_sql);
$stmt->bind_param("iiii", $patientId, $counselorId, $counselorId, $patientId);
$stmt->execute();
$res = $stmt->get_result();
echo "Valid Appointment Found: " . ($res->num_rows > 0 ? "YES" : "NO") . "\n\n";

// 2. Verify Security Check for unauthorized user
echo "Checking appointment between 1004 and 2 (Expected NO)...\n";
$stmt->bind_param("iiii", $patientId, $randomUserId, $randomUserId, $patientId);
$stmt->execute();
$res = $stmt->get_result();
echo "Valid Appointment Found: " . ($res->num_rows > 0 ? "YES (Wait, check data)" : "NO (Correct)") . "\n\n";

// 3. Verify Thread Logic SQL
echo "Checking Chat Thread SQL logic for Counselor 39...\n";
$thread_sql = "
    SELECT u.full_name
    FROM users u
    AND EXISTS (
        SELECT 1 FROM appointments a 
        WHERE (a.patient_id = u.id AND a.counselor_id = ?) 
           OR (a.patient_id = ? AND a.counselor_id = u.id)
        AND a.status IN ('Confirmed', 'Pending', 'Completed')
    )
    WHERE u.id != ? AND u.id = ?
";
// (Simplifying for verification)
$test_sql = "SELECT full_name FROM users u WHERE u.id = ? AND EXISTS (SELECT 1 FROM appointments a WHERE (a.patient_id = u.id AND a.counselor_id = ?) AND a.status IN ('Confirmed', 'Pending', 'Completed'))";
$stmt2 = $conn->prepare($test_sql);
$stmt2->bind_param("ii", $patientId, $counselorId);
$stmt2->execute();
$res2 = $stmt2->get_result();
echo "Patient 1004 visible in Counselor 39 threads: " . ($res2->num_rows > 0 ? "YES" : "NO") . "\n";

echo "\n--- Verification Complete ---";
?>
