<?php
// Test Script for Appointment Completion and Payment Flow

// 1. Setup: Create a Confirmed Appointment
include 'db_connect.php';

// Create Users
$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('Test Patient', 'test_p_pay@example.com', 'hash', 'Patient')");
$p_id = $conn->insert_id;
$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('Test Counselor', 'test_c_pay@example.com', 'hash', 'Counselor')");
$c_id = $conn->insert_id;

// Create Confirmed Appointment
$conn->query("INSERT INTO appointments (patient_id, counselor_id, appointment_date, time_slot, status) VALUES ($p_id, $c_id, '2026-03-01', '10:00 AM', 'Confirmed')");
$app_id = $conn->insert_id;

echo "1. Setup Complete. Appointment ID: $app_id (Confirmed)\n";

// 2. Simulate Video Call End (Complete Appointment)
$url = 'http://localhost/genecare/complete_appointment.php';
$data = ['appointment_id' => $app_id];
$options = [
    'http' => [
        'header'  => "Content-type: application/json\r\n",
        'method'  => 'POST',
        'content' => json_encode($data),
    ],
];
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
echo "2. Complete Appointment Response: $result\n";

// Verify Status is Completed
$res = $conn->query("SELECT status FROM appointments WHERE id = $app_id");
$row = $res->fetch_assoc();
if ($row['status'] == 'Completed') {
    echo "   -> Appointment Status verified: Completed\n";
} else {
    echo "   -> FAIL: Status is " . $row['status'] . "\n";
}

// 3. Simulate Payment Creation
$url = 'http://localhost/genecare/create_payment.php';
$data = ['appointment_id' => $app_id, 'amount' => 1500.00, 'payment_method' => 'UPI'];
$options['http']['content'] = json_encode($data);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
echo "3. Create Payment Response: $result\n";
$json = json_decode($result, true);
$payment_id = $json['payment_id'] ?? null;

if ($payment_id) {
    echo "   -> Payment ID: $payment_id\n";
} else {
    echo "   -> FAIL: No Payment ID\n";
    exit();
}

// 4. Simulate Payment Success
$url = 'http://localhost/genecare/update_payment_status.php';
$data = ['payment_id' => $payment_id, 'status' => 'Completed', 'transaction_id' => 'TXN12345'];
$options['http']['content'] = json_encode($data);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
echo "4. Update Payment Status Response: $result\n";

// Verify Payment Status
$res = $conn->query("SELECT status, transaction_id FROM payments WHERE id = $payment_id");
$row = $res->fetch_assoc();
if ($row['status'] == 'Completed' && $row['transaction_id'] == 'TXN12345') {
    echo "   -> Payment Status verified: Completed\n";
} else {
    echo "   -> FAIL: Payment Status is " . $row['status'] . "\n";
}

// Cleanup
$conn->query("DELETE FROM payments WHERE id = $payment_id");
$conn->query("DELETE FROM appointments WHERE id = $app_id");
$conn->query("DELETE FROM users WHERE id IN ($p_id, $c_id)");
echo "5. Cleanup Complete.\n";

$conn->close();
?>
