<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);
$email = $_POST['email'] ?? $data['email'] ?? null;
$new_password = $_POST['new_password'] ?? $data['new_password'] ?? null;

if (empty($email) || empty($new_password)) {
    echo json_encode(["status" => "error", "message" => "Email and new password are required"]);
    exit();
}

// Strong password validation (same rules as registration)
$password_errors = [];
if (strlen($new_password) < 8) {
    $password_errors[] = "at least 8 characters";
}
if (!preg_match('/[A-Z]/', $new_password)) {
    $password_errors[] = "an uppercase letter";
}
if (!preg_match('/[a-z]/', $new_password)) {
    $password_errors[] = "a lowercase letter";
}
if (!preg_match('/[0-9]/', $new_password)) {
    $password_errors[] = "a digit";
}
if (!preg_match('/[^A-Za-z0-9]/', $new_password)) {
    $password_errors[] = "a special character";
}
if (!empty($password_errors)) {
    echo json_encode(["status" => "error", "message" => "Password must contain: " . implode(", ", $password_errors)]);
    exit();
}

// Verify that OTP was used (validated) for this email recently
$otp_stmt = $conn->prepare("SELECT id FROM password_resets WHERE email = ? AND used = TRUE ORDER BY created_at DESC LIMIT 1");
$otp_stmt->bind_param("s", $email);
$otp_stmt->execute();
$otp_result = $otp_stmt->get_result();

if ($otp_result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Please verify your OTP first"]);
    $otp_stmt->close();
    $conn->close();
    exit();
}
$otp_stmt->close();

// Update password
$password_hash = password_hash($new_password, PASSWORD_DEFAULT);
$stmt = $conn->prepare("UPDATE users SET password_hash = ? WHERE email = ?");
$stmt->bind_param("ss", $password_hash, $email);

if ($stmt->execute() && $stmt->affected_rows > 0) {
    // Clean up used OTPs
    $cleanup = $conn->prepare("DELETE FROM password_resets WHERE email = ?");
    $cleanup->bind_param("s", $email);
    $cleanup->execute();
    $cleanup->close();

    echo json_encode(["status" => "success", "message" => "Password reset successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to reset password. User not found"]);
}

$stmt->close();
$conn->close();
?>
