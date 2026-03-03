<?php
include 'db_connect.php';

// 1. Create a dummy counselor profile if not exists
$user_id = 888;
$conn->query("INSERT INTO users (id, full_name, email, password, user_type) VALUES ($user_id, 'Test Counselor 2', 'test2@test.com', 'pass', 'Counselor') ON DUPLICATE KEY UPDATE full_name='Test Counselor 2'");
$conn->query("INSERT INTO counselor_profiles (user_id, specialization, consultation_fee) VALUES ($user_id, 'Test Spec', 600) ON DUPLICATE KEY UPDATE specialization='Test Spec'");
$conn->query("INSERT INTO counselor_qualifications (user_id, status) VALUES ($user_id, 'Approved') ON DUPLICATE KEY UPDATE status='Approved'");

// 2. Fetch
$url = 'http://localhost/genecare/get_counselors.php';
$result = file_get_contents($url);

echo "Result: " . $result . "\n";

// Cleanup
$conn->query("DELETE FROM users WHERE id = $user_id");
$conn->query("DELETE FROM counselor_profiles WHERE user_id = $user_id");
$conn->query("DELETE FROM counselor_qualifications WHERE user_id = $user_id");
?>
