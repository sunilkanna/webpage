<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$admin_id = $_POST['admin_id'] ?? $data['admin_id'] ?? null;
$counselor_id = $_POST['counselor_id'] ?? $data['counselor_id'] ?? null;
$status = $_POST['status'] ?? $data['status'] ?? null; // Approved, Rejected
$rejection_reason = $_POST['rejection_reason'] ?? $data['rejection_reason'] ?? null;

if (empty($admin_id) || empty($counselor_id) || empty($status)) {
    echo json_encode(["status" => "error", "message" => "Admin ID, counselor ID, and status are required"]);
    exit();
}

$stmt = $conn->prepare("UPDATE counselor_qualifications SET status = ?, rejection_reason = ?, verified_by = ?, verified_at = CURRENT_TIMESTAMP WHERE user_id = ?");
$stmt->bind_param("ssii", $status, $rejection_reason, $admin_id, $counselor_id);

if ($stmt->execute()) {
    // Create notification for counselor
    $title = "Verification Update";
    $message = "Your counselor verification status has been updated to $status.";
    $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Verification')");
    $notify->bind_param("iss", $counselor_id, $title, $message);
    $notify->execute();
    $notify->close();

    echo json_encode(["status" => "success", "message" => "Counselor verification status updated to $status"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
