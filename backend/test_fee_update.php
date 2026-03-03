<?php
include 'db_connect.php';

// 1. Create a dummy counselor profile if not exists
$user_id = 999;
$conn->query("INSERT INTO users (id, full_name, email, password, user_type) VALUES ($user_id, 'Test Counselor', 'test@test.com', 'pass', 'Counselor') ON DUPLICATE KEY UPDATE full_name='Test Counselor'");
$conn->query("INSERT INTO counselor_profiles (user_id, consultation_fee) VALUES ($user_id, 500) ON DUPLICATE KEY UPDATE consultation_fee=500");

// 2. Test Update
$url = 'http://localhost/genecare/update_consultation_fee.php';
$data = ['user_id' => $user_id, 'consultation_fee' => 750];

$options = [
    'http' => [
        'header'  => "Content-type: application/json\r\n",
        'method'  => 'POST',
        'content' => json_encode($data),
    ],
];
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

echo "Update Result: " . $result . "\n";

// 3. Verify
$res = $conn->query("SELECT consultation_fee FROM counselor_profiles WHERE user_id = $user_id");
$row = $res->fetch_assoc();
echo "New Fee in DB: " . $row['consultation_fee'] . "\n";

// Cleanup
$conn->query("DELETE FROM users WHERE id = $user_id");
$conn->query("DELETE FROM counselor_profiles WHERE user_id = $user_id");
?>
