<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$sender_id = $_POST['sender_id'] ?? $data['sender_id'] ?? null;
$receiver_id = $_POST['receiver_id'] ?? $data['receiver_id'] ?? null;
$message_text = $_POST['message_text'] ?? $data['message_text'] ?? null;

if (empty($sender_id) || empty($receiver_id) || empty($message_text)) {
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit();
}

// Check for valid appointment between sender and receiver
$check_sql = "SELECT id FROM appointments 
              WHERE ((patient_id = ? AND counselor_id = ?) OR (patient_id = ? AND counselor_id = ?)) 
              AND status IN ('Confirmed', 'Pending', 'Completed') 
              LIMIT 1";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("iiii", $sender_id, $receiver_id, $receiver_id, $sender_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Unauthorized: No active session or request found between users."]);
    $check_stmt->close();
    exit();
}
$check_stmt->close();

$stmt = $conn->prepare("INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)");
$stmt->bind_param("iis", $sender_id, $receiver_id, $message_text);

if ($stmt->execute()) {
    $message_id = $conn->insert_id;
    
    // Get sender name for notification
    $s_stmt = $conn->prepare("SELECT full_name FROM users WHERE id = ?");
    $s_stmt->bind_param("i", $sender_id);
    $s_stmt->execute();
    $s_result = $s_stmt->get_result();
    $sender = $s_result->fetch_assoc();
    $sender_name = $sender['full_name'] ?? 'Someone';
    $s_stmt->close();

    // Create notification for receiver
    $title = "New Message";
    $message = "You have a new message from $sender_name.";
    $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Message')");
    $notify->bind_param("iss", $receiver_id, $title, $message);
    $notify->execute();
    $notify->close();

    echo json_encode(["status" => "success", "message_id" => (int)$message_id]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
