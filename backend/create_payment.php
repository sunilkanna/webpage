<?php
include_once 'db_connect.php';
include_once 'razorpay_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$appointment_id = $data['appointment_id'] ?? null;
$amount = $data['amount'] ?? null; // Amount in Rupees
$payment_method = $data['payment_method'] ?? 'UPI';

if (!$appointment_id || !$amount) {
    echo json_encode(["status" => "error", "message" => "Appointment ID and Amount required"]);
    exit();
}

$amount_in_paise = (int)($amount * 100);
$receipt = "receipt_appt_" . $appointment_id . "_" . time();

// Create Razorpay Order via cURL
$order_id = null;
if (defined('RAZORPAY_KEY_ID') && strpos(RAZORPAY_KEY_ID, 'YOUR_KEY_ID') === false) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "https://api.razorpay.com/v1/orders");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
        "amount" => $amount_in_paise,
        "currency" => "INR",
        "receipt" => $receipt,
        "payment_capture" => 1
    ]));
    curl_setopt($ch, CURLOPT_USERPWD, RAZORPAY_KEY_ID . ":" . RAZORPAY_KEY_SECRET);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http_code == 200) {
        $result = json_decode($response, true);
        $order_id = $result['id'];
    } else {
        echo json_encode(["status" => "error", "message" => "Razorpay Order Creation Failed: " . $response]);
        exit();
    }
    } else {
        echo json_encode(["status" => "error", "message" => "Razorpay API Keys are not configured in razorpay_config.php. Please add your real key_id and key_secret."]);
        exit();
    }

$stmt = $conn->prepare("INSERT INTO payments (appointment_id, razorpay_order_id, amount, payment_method, status) VALUES (?, ?, ?, ?, 'Pending')");
$stmt->bind_param("isds", $appointment_id, $order_id, $amount, $payment_method);

if ($stmt->execute()) {
    $payment_id = $stmt->insert_id;
    echo json_encode([
        "status" => "success", 
        "payment_id" => $payment_id, 
        "razorpay_order_id" => $order_id,
        "amount" => $amount_in_paise, // SDK needs amount in paise
        "key_id" => RAZORPAY_KEY_ID,
        "message" => "Payment order created successfully"
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to save payment record: " . $conn->error]);
}

$stmt->close();
$conn->close();
?>
