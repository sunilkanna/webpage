<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);
$email = $_POST['email'] ?? $data['email'] ?? null;
$otp = $_POST['otp'] ?? $data['otp'] ?? null;

if (empty($email) || empty($otp)) {
    echo json_encode(["status" => "error", "message" => "Email and OTP are required"]);
    exit();
}

// Verify OTP
$stmt = $conn->prepare("SELECT id FROM password_resets WHERE email = ? AND otp = ? AND expires_at > NOW() AND used = FALSE");
$stmt->bind_param("ss", $email, $otp);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Mark OTP as used
    $update_stmt = $conn->prepare("UPDATE password_resets SET used = TRUE WHERE email = ? AND otp = ?");
    $update_stmt->bind_param("ss", $email, $otp);
    $update_stmt->execute();
    $update_stmt->close();

    echo json_encode(["status" => "success", "message" => "OTP verified successfully"]);
} else {
    // Check if expired
    $exp_stmt = $conn->prepare("SELECT id FROM password_resets WHERE email = ? AND otp = ? AND expires_at <= NOW()");
    $exp_stmt->bind_param("ss", $email, $otp);
    $exp_stmt->execute();
    $exp_result = $exp_stmt->get_result();

    if ($exp_result->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "OTP has expired. Please request a new code"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid verification code"]);
    }
    $exp_stmt->close();
}

$stmt->close();
$conn->close();
?>
