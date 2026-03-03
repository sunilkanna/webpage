<?php
include 'db_connect.php';

$user_id = $_GET['user_id'];
$other_user_id = $_GET['other_user_id'];

// Check for valid appointment between users
$check_sql = "SELECT id FROM appointments 
              WHERE ((patient_id = ? AND counselor_id = ?) OR (patient_id = ? AND counselor_id = ?)) 
              AND status IN ('Confirmed', 'Pending', 'Completed') 
              LIMIT 1";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("iiii", $user_id, $other_user_id, $other_user_id, $user_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Unauthorized: No active session or request found between users."]);
    $check_stmt->close();
    exit();
}
$check_stmt->close();

$sql = "SELECT * FROM messages 
        WHERE (sender_id = ? AND receiver_id = ?) 
           OR (sender_id = ? AND receiver_id = ?) 
        ORDER BY sent_at ASC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("iiii", $user_id, $other_user_id, $other_user_id, $user_id);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
while ($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['sender_id'] = (int)$row['sender_id'];
    $row['receiver_id'] = (int)$row['receiver_id'];
    $row['is_read'] = (bool)$row['is_read'];
    $messages[] = $row;
}

echo json_encode(["status" => "success", "messages" => $messages]);

$stmt->close();
$conn->close();
?>
