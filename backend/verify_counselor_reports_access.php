<?php
include 'db_connect.php';

echo "<h2>Verifying Counselor Reports Access</h2>";

// 1. Create dummy data
$counselor_id_a = 998;
$counselor_id_b = 999;
$patient_id = 1; // Assuming patient 1 exists

// Cleanup
$conn->query("DELETE FROM users WHERE id IN ($counselor_id_a, $counselor_id_b)");
$conn->query("DELETE FROM appointments WHERE counselor_id IN ($counselor_id_a, $counselor_id_b)");

// Insert test users
$conn->query("INSERT INTO users (id, full_name, email, password_hash, user_type) VALUES ($counselor_id_a, 'Counselor A', 'a@test.com', 'hash', 'Counselor')");
$conn->query("INSERT INTO users (id, full_name, email, password_hash, user_type) VALUES ($counselor_id_b, 'Counselor B', 'b@test.com', 'hash', 'Counselor')");

// Insert test appointment with report for A
$date = date('Y-m-d');
$conn->query("INSERT INTO appointments (patient_id, counselor_id, appointment_date, time_slot, status, medical_report_url) 
              VALUES ($patient_id, $counselor_id_a, '$date', '10:00 AM', 'Pending', 'reports/report_a.pdf')");

// Insert test appointment with report for B
$conn->query("INSERT INTO appointments (patient_id, counselor_id, appointment_date, time_slot, status, medical_report_url) 
              VALUES ($patient_id, $counselor_id_b, '$date', '11:00 AM', 'Pending', 'reports/report_b.pdf')");

function checkReports($cid) {
    global $conn;
    $url = "http://localhost/genecare/get_counselor_reports.php?counselor_id=" . $cid;
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

// Test Counselor A
$res_a = checkReports($counselor_id_a);
if ($res_a['status'] === 'success' && count($res_a['reports']) === 1 && $res_a['reports'][0]['fileUrl'] === 'reports/report_a.pdf') {
    echo "<p style='color: green;'>PASS: Counselor A sees their own report correctly.</p>";
} else {
    echo "<p style='color: red;'>FAIL: Counselor A report check failed.</p>";
    print_r($res_a);
}

// Test Counselor B
$res_b = checkReports($counselor_id_b);
if ($res_b['status'] === 'success' && count($res_b['reports']) === 1 && $res_b['reports'][0]['fileUrl'] === 'reports/report_b.pdf') {
    echo "<p style='color: green;'>PASS: Counselor B sees their own report correctly.</p>";
} else {
    echo "<p style='color: red;'>FAIL: Counselor B report check failed.</p>";
    print_r($res_b);
}

// Cleanup
$conn->query("DELETE FROM appointments WHERE counselor_id IN ($counselor_id_a, $counselor_id_b)");
$conn->query("DELETE FROM users WHERE id IN ($counselor_id_a, $counselor_id_b)");

$conn->close();
?>
