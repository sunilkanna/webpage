<?php
include 'db_connect.php';

$appointment_id = $_GET['appointment_id'] ?? null;

if (!$appointment_id) {
    echo json_encode(["status" => "error", "message" => "Appointment ID required"]);
    exit();
}

$stmt = $conn->prepare("SELECT status, meeting_link, session_start_time FROM appointments WHERE id = ?");
$stmt->bind_param("i", $appointment_id);
$stmt->execute();
$result = $stmt->get_result();
$appointment = $result->fetch_assoc();
$stmt->close();

if (!$appointment) {
    echo json_encode(["status" => "error", "message" => "Appointment not found"]);
    exit();
}

$session_started = !empty($appointment['session_start_time']);

echo json_encode([
    "status" => "success",
    "appointment_status" => $appointment['status'],
    "session_started" => $session_started,
    "meeting_link" => $appointment['meeting_link']
]);

$conn->close();
?>
