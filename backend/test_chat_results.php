<?php
include 'db_connect.php';

// Setup Test Data
$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('Chat Patient', 'chat_p@example.com', 'hash', 'Patient')");
$p_id = $conn->insert_id;
$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('Chat Counselor', 'chat_c@example.com', 'hash', 'Counselor')");
$c_id = $conn->insert_id;
$conn->query("INSERT INTO patient_profiles (user_id, profile_image_url) VALUES ($p_id, 'http://example.com/p.jpg')");

// Create Chat Message
$conn->query("INSERT INTO messages (sender_id, receiver_id, message_text, is_read) VALUES ($p_id, $c_id, 'Hello Counselor', 0)");

// Create Risk Assessment
$conn->query("INSERT INTO risk_assessments (patient_id, risk_score, risk_category, assessed_at) VALUES ($p_id, 25, 'Low', '2026-03-01 10:00:00')");

// Create Report
$conn->query("INSERT INTO patient_reports (patient_id, file_name, file_url, uploaded_at) VALUES ($p_id, 'TestReport.pdf', 'http://example.com/report.pdf', '2026-03-02 10:00:00')");

echo "1. Setup Complete.\n";

// Test 1: Get Chat Threads for Counselor
$url = "http://localhost/genecare/get_chat_threads.php?counselor_id=$c_id";
$res = file_get_contents($url);
echo "2. Chat Threads Response: $res\n";
$json = json_decode($res, true);
if (!empty($json['threads']) && $json['threads'][0]['senderName'] == 'Chat Patient' && $json['threads'][0]['unreadCount'] == 1) {
    echo "   -> PASS: Chat Thread verified.\n";
} else {
    echo "   -> FAIL: Chat Thread mismatch.\n";
}

// Test 2: Get Patient Results for Patient
$url = "http://localhost/genecare/get_patient_results.php?patient_id=$p_id";
$res = file_get_contents($url);
echo "3. Patient Results Response: $res\n";
$json = json_decode($res, true);

$risk_ok = ($json['risk_assessment']['risk_score'] == 25);
$reports_ok = (count($json['reports']) >= 2); // 1 report + 1 risk history item

if ($risk_ok && $reports_ok) {
    echo "   -> PASS: Results verified.\n";
} else {
    echo "   -> FAIL: Results mismatch.\n";
}

// Cleanup
$conn->query("DELETE FROM messages WHERE sender_id = $p_id");
$conn->query("DELETE FROM risk_assessments WHERE patient_id = $p_id");
$conn->query("DELETE FROM patient_reports WHERE patient_id = $p_id");
$conn->query("DELETE FROM patient_profiles WHERE user_id = $p_id");
$conn->query("DELETE FROM users WHERE id IN ($p_id, $c_id)");

echo "4. Cleanup Complete.\n";
$conn->close();
?>
