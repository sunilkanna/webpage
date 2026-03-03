<?php
include 'db_connect.php';
include 'email_config.php';

// PHPMailer
require 'PHPMailer-6.9.1/src/Exception.php';
require 'PHPMailer-6.9.1/src/PHPMailer.php';
require 'PHPMailer-6.9.1/src/SMTP.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

$data = json_decode(file_get_contents("php://input"), true);
$email = $_POST['email'] ?? $data['email'] ?? null;

if (empty($email)) {
    echo json_encode(["status" => "error", "message" => "Email is required"]);
    exit();
}

// Check if user exists
$stmt = $conn->prepare("SELECT id, full_name FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "No account found with this email"]);
    $stmt->close();
    $conn->close();
    exit();
}

$user = $result->fetch_assoc();
$user_name = $user['full_name'];
$stmt->close();

// Generate 5-digit OTP
$otp = str_pad(rand(0, 99999), 5, '0', STR_PAD_LEFT);

// Create OTP table if it doesn't exist
$conn->query("CREATE TABLE IF NOT EXISTS password_resets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");

// Delete any previous OTPs for this email
$del_stmt = $conn->prepare("DELETE FROM password_resets WHERE email = ?");
$del_stmt->bind_param("s", $email);
$del_stmt->execute();
$del_stmt->close();

// Store new OTP
$ins_stmt = $conn->prepare("INSERT INTO password_resets (email, otp, expires_at) VALUES (?, ?, NOW() + INTERVAL 10 MINUTE)");
$ins_stmt->bind_param("ss", $email, $otp);

if (!$ins_stmt->execute()) {
    echo json_encode(["status" => "error", "message" => "Failed to generate OTP"]);
    $ins_stmt->close();
    $conn->close();
    exit();
}
$ins_stmt->close();

// Send OTP via email using PHPMailer
$mail = new PHPMailer(true);

try {
    // SMTP Configuration
    $mail->isSMTP();
    $mail->Host       = SMTP_HOST;
    $mail->SMTPAuth   = true;
    $mail->Username   = SMTP_USERNAME;
    $mail->Password   = SMTP_PASSWORD;
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
    $mail->Port       = 465;
    $mail->SMTPOptions = array(
        'ssl' => array(
            'verify_peer' => false,
            'verify_peer_name' => false,
            'allow_self_signed' => true
        )
    );

    // Recipients
    $mail->setFrom(SMTP_FROM_EMAIL, SMTP_FROM_NAME);
    $mail->addAddress($email, $user_name);

    // Email content
    $mail->isHTML(true);
    $mail->Subject = 'GeneCare - Password Reset Code';
    $mail->Body = '
    <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
        <div style="text-align: center; padding: 20px; background: linear-gradient(135deg, #008080, #00b3b3); border-radius: 12px 12px 0 0;">
            <h1 style="color: white; margin: 0; font-size: 24px;">GeneCare</h1>
            <p style="color: rgba(255,255,255,0.9); margin: 5px 0 0;">Password Reset</p>
        </div>
        <div style="padding: 30px; background: #ffffff; border: 1px solid #e0e0e0;">
            <p style="color: #333; font-size: 16px;">Hi <strong>' . htmlspecialchars($user_name) . '</strong>,</p>
            <p style="color: #555; font-size: 14px;">You requested a password reset for your GeneCare account. Use the verification code below:</p>
            <div style="text-align: center; margin: 25px 0;">
                <div style="display: inline-block; background: #f5f5f5; border: 2px dashed #008080; border-radius: 8px; padding: 15px 30px;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #008080;">' . $otp . '</span>
                </div>
            </div>
            <p style="color: #555; font-size: 14px;">This code will expire in <strong>10 minutes</strong>.</p>
            <p style="color: #999; font-size: 12px;">If you didn\'t request this, you can safely ignore this email.</p>
        </div>
        <div style="text-align: center; padding: 15px; background: #f9f9f9; border-radius: 0 0 12px 12px; border: 1px solid #e0e0e0; border-top: none;">
            <p style="color: #999; font-size: 11px; margin: 0;">&copy; ' . date('Y') . ' GeneCare. All rights reserved.</p>
        </div>
    </div>';
    $mail->AltBody = "Hi $user_name, your GeneCare password reset code is: $otp. This code expires in 10 minutes.";

    $mail->send();
    echo json_encode([
        "status" => "success",
        "message" => "Verification code sent to your email"
    ]);
} catch (Exception $e) {
    // Email failed - return error with details for debugging
    echo json_encode([
        "status" => "error",
        "message" => "Failed to send email. Please check SMTP configuration.",
        "debug" => $mail->ErrorInfo
    ]);
}

$conn->close();
?>
