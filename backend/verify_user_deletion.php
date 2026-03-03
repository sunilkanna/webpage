<?php
include 'db_connect.php';

// 1. Create a test user
$full_name = "Test User For Deletion";
$email = "test_delete_" . time() . "@example.com";
$password_hash = password_hash("password123", PASSWORD_DEFAULT);
$user_type = "Patient";

$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('$full_name', '$email', '$password_hash', '$user_type')");
$user_id = $conn->insert_id;

echo "Created test user with ID: $user_id\n";

// 2. Add some dummy data for this user
$conn->query("INSERT INTO patient_reports (patient_id, file_name, file_url) VALUES ($user_id, 'test_cleanup.pdf', 'http://example.com/test.pdf')");
$conn->query("INSERT INTO risk_assessments (patient_id, risk_score, risk_category) VALUES ($user_id, 25, 'Medium')");
$conn->query("INSERT INTO appointments (patient_id, counselor_id, appointment_date, time_slot, status) VALUES ($user_id, 1, '2026-03-03', '10:00 AM', 'Pending')");

echo "Added dummy reports, assessments, and appointments for user $user_id.\n";

// 3. Call manage_user.php to delete
$ch = curl_init('http://localhost/genecare/manage_user.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
    'user_id' => $user_id,
    'action' => 'delete'
]));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);

$response = curl_exec($ch);
curl_close($ch);

echo "Response from manage_user.php: $response\n\n";

// 4. Verify cleanup
$user_check = $conn->query("SELECT * FROM users WHERE id = $user_id")->num_rows;
$report_check = $conn->query("SELECT * FROM patient_reports WHERE patient_id = $user_id")->num_rows;
$assessment_check = $conn->query("SELECT * FROM risk_assessments WHERE patient_id = $user_id")->num_rows;
$appointment_check = $conn->query("SELECT * FROM appointments WHERE patient_id = $user_id")->num_rows;

echo "Verification Results:\n";
echo "Users remaining: $user_check (Expected: 0)\n";
echo "Reports remaining: $report_check (Expected: 0)\n";
echo "Assessments remaining: $assessment_check (Expected: 0)\n";
echo "Appointments remaining: $appointment_check (Expected: 0)\n";

if ($user_check === 0 && $report_check === 0 && $assessment_check === 0 && $appointment_check === 0) {
    echo "\nSUCCESS: User and all related data cleaned up correctly!\n";
} else {
    echo "\nFAILURE: Some data remains in the database.\n";
}

$conn->close();
?>
