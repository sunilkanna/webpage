<?php
include_once 'db_connect.php';
include_once 'razorpay_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$payment_id = $data['payment_id'] ?? null; // Internal payment record ID
$razorpay_order_id = $data['razorpay_order_id'] ?? null;
$razorpay_payment_id = $data['razorpay_payment_id'] ?? null;
$razorpay_signature = $data['razorpay_signature'] ?? null;

if (!$razorpay_payment_id || !$razorpay_signature || !$razorpay_order_id) {
    echo json_encode(["status" => "error", "message" => "Missing required Razorpay parameters"]);
    exit();
}

// Security: Verify Signature
$expected_signature = hash_hmac('sha256', $razorpay_order_id . '|' . $razorpay_payment_id, RAZORPAY_KEY_SECRET);

if ($expected_signature === $razorpay_signature) {
    // Correct Signature - Complete the payment
    $conn->begin_transaction();
    try {
        // 1. Update Payment Record
        $stmt = $conn->prepare("UPDATE payments SET status = 'Completed', razorpay_payment_id = ?, razorpay_signature = ? WHERE razorpay_order_id = ?");
        $stmt->bind_param("sss", $razorpay_payment_id, $razorpay_signature, $razorpay_order_id);
        $stmt->execute();

        // 2. Fetch appointment_id to update status
        $q = $conn->prepare("SELECT appointment_id FROM payments WHERE razorpay_order_id = ?");
        $q->bind_param("s", $razorpay_order_id);
        $q->execute();
        $res = $q->get_result();
        $appt = $res->fetch_assoc();

        if ($appt) {
            $appointment_id = $appt['appointment_id'];
            // 3. Mark Appointment as Completed
            $u = $conn->prepare("UPDATE appointments SET status = 'Completed' WHERE id = ?");
            $u->bind_param("i", $appointment_id);
            $u->execute();

            // 4. Create a success notification
            $notif = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) SELECT patient_id, 'Payment Successful', 'Your session payment has been confirmed.', 'Payment' FROM appointments WHERE id = ?");
            $notif->bind_param("i", $appointment_id);
            $notif->execute();
        }

        $conn->commit();
        echo json_encode(["status" => "success", "message" => "Payment verified and booking completed"]);
        
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["status" => "error", "message" => "Verification succeeded but DB update failed: " . $e->getMessage()]);
    }
} else {
    // Fraud Detection / Signature Mismatch
    $stmt = $conn->prepare("UPDATE payments SET status = 'Failed' WHERE razorpay_order_id = ?");
    $stmt->bind_param("s", $razorpay_order_id);
    $stmt->execute();
    
    echo json_encode(["status" => "error", "message" => "Signature Verification Failed. Potential tampering detected."]);
}

$conn->close();
?>
