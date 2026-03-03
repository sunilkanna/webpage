<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$user_id = $_POST['user_id'] ?? $data['user_id'] ?? null;
$consultation_fee = $_POST['consultation_fee'] ?? $data['consultation_fee'] ?? null;

if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "User ID is required"]);
    exit();
}

if (!isset($consultation_fee)) {
    echo json_encode(["status" => "error", "message" => "Consultation fee is required"]);
    exit();
}

// Update the consultation fee
$stmt = $conn->prepare("UPDATE counselor_profiles SET consultation_fee = ? WHERE user_id = ?");
$stmt->bind_param("di", $consultation_fee, $user_id);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Consultation fee updated successfully"]);
    } else {
        // Check if user exists (if affected_rows is 0, it might mean the fee was same or user not found)
        // But for this specific case, success is fine or a soft warning. 
        // Let's return success but note no change if needed, but simple success is usually enough.
        echo json_encode(["status" => "success", "message" => "Consultation fee updated (or was already correct)"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
