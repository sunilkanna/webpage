<?php
header('Content-Type: application/json');
include_once 'db_connect.php';

$data = json_decode(file_get_contents("php://input"));
$notification_id = $data->notification_id ?? null;

if (!$notification_id) {
    echo json_encode(["status" => "error", "message" => "Notification ID required"]);
    exit();
}

try {
    $stmt = $conn->prepare("DELETE FROM notifications WHERE id = ?");
    $stmt->bind_param("i", $notification_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode(["status" => "success", "message" => "Notification deleted"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Notification not found or already deleted"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to delete notification"]);
    }
    $stmt->close();
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Server Error: " . $e->getMessage()]);
}

$conn->close();
?>
