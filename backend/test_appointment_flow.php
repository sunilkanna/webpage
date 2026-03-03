<?php
include 'db_connect.php';

// Setup Test Data
$patient_id = 991;
$counselor_id = 992;
$date = date('Y-m-d', strtotime('+1 day'));
$time = "10:00 AM";

// Cleanup
$conn->query("DELETE FROM appointments WHERE patient_id = $patient_id");
$conn->query("DELETE FROM users WHERE id IN ($patient_id, $counselor_id)");

// Create Users
$conn->query("INSERT INTO users (id, full_name, email, password_hash, user_type) VALUES ($patient_id, 'Test Patient', 'tp@test.com', 'hash', 'Patient')");
$conn->query("INSERT INTO users (id, full_name, email, password_hash, user_type) VALUES ($counselor_id, 'Test Counselor', 'tc@test.com', 'hash', 'Counselor')");

echo "1. Users Created.\n";

// 1. Book Appointment (Pending)
$url = 'http://localhost/genecare/book_appointment.php';
$data = ['patient_id' => $patient_id, 'counselor_id' => $counselor_id, 'date' => $date, 'time' => $time];
$options = [
    'http' => [
        'header'  => "Content-type: application/json\r\n",
        'method'  => 'POST',
        'content' => json_encode($data),
    ],
];
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
$json = json_decode($result, true);
$app_id = $json['appointment_id'] ?? null;

if ($json['status'] == 'success' && $app_id) {
    echo "2. Booking Successful. ID: $app_id. Status: Pending (Default)\n";
} else {
    echo "2. Booking FAILED. " . $result . "\n";
    exit;
}

// 2. Verify Counselor sees Pending Request
$url = "http://localhost/genecare/get_counselor_appointments.php?counselor_id=$counselor_id";
$result = file_get_contents($url);
$data = json_decode($result, true);
$found = false;
foreach ($data['appointments'] as $app) {
    if ($app['id'] == $app_id && $app['status'] == 'Pending') {
        $found = true;
        break;
    }
}
if ($found) echo "3. Counselor sees Pending Appointment: PASS\n";
else echo "3. Counselor sees Pending Appointment: FAIL\n";

// 3. Verify Patient sees Pending Status
$url = 'http://localhost/genecare/get_dashboard_stats.php';
$data = ['user_id' => $patient_id, 'user_type' => 'Patient'];
$options['http']['content'] = json_encode($data);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
$json = json_decode($result, true);
$patient_app = $json['patient_stats']['upcoming_appointment'] ?? null;

if ($patient_app && $patient_app['id'] == $app_id && $patient_app['status'] == 'Pending') {
    echo "4. Patient sees Pending Status: PASS\n";
} else {
    echo "4. Patient sees Pending Status: FAIL. " . json_encode($patient_app) . "\n";
}

// 4. Counselor Accepts (Update to Confirmed)
$url = 'http://localhost/genecare/update_appointment_status.php';
$data = ['appointment_id' => $app_id, 'status' => 'Confirmed'];
$options['http']['content'] = json_encode($data);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

// 5. Verify Patient sees Confirmed Status
$url = 'http://localhost/genecare/get_dashboard_stats.php';
$data = ['user_id' => $patient_id, 'user_type' => 'Patient'];
$options['http']['content'] = json_encode($data);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
$json = json_decode($result, true);
$patient_app = $json['patient_stats']['upcoming_appointment'] ?? null;

if ($patient_app && $patient_app['id'] == $app_id && $patient_app['status'] == 'Confirmed') {
    echo "5. Patient sees Confirmed Status: PASS\n";
} else {
    echo "5. Patient sees Confirmed Status: FAIL. " . json_encode($patient_app) . "\n";
}

// Cleanup
$conn->query("DELETE FROM appointments WHERE patient_id = $patient_id");
$conn->query("DELETE FROM users WHERE id IN ($patient_id, $counselor_id)");
echo "6. Cleanup Done.\n";
?>
