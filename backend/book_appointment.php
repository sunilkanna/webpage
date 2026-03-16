<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$patient_id = $_POST['patient_id'] ?? $data['patient_id'] ?? null;
$counselor_id = $_POST['counselor_id'] ?? $data['counselor_id'] ?? null;
$date = $_POST['date'] ?? $data['date'] ?? null; // Format: YYYY-MM-DD
$time = $_POST['time'] ?? $data['time'] ?? null;

if (empty($patient_id) || empty($counselor_id) || empty($date) || empty($time)) {
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit();
}

// Validate that the appointment date is not in the past
$today = date('Y-m-d');
if ($date < $today) {
    echo json_encode(["status" => "error", "message" => "Cannot book appointments for past dates"]);
    exit();
}

// Check availability
$check = $conn->prepare("SELECT id FROM appointments WHERE counselor_id = ? AND appointment_date = ? AND time_slot = ? AND status != 'Cancelled'");
$check->bind_param("iss", $counselor_id, $date, $time);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Slot already booked"]);
    exit();
}
$check->close();

$medical_report_url = $_POST['medical_report_url'] ?? $data['medical_report_url'] ?? null;

$stmt = $conn->prepare("INSERT INTO appointments (patient_id, counselor_id, appointment_date, time_slot, status, medical_report_url) VALUES (?, ?, ?, ?, 'Pending', ?)");
$stmt->bind_param("iisss", $patient_id, $counselor_id, $date, $time, $medical_report_url);

if ($stmt->execute()) {
    $appointment_id = $stmt->insert_id;
    
    // Get patient name for notification
    $p_stmt = $conn->prepare("SELECT full_name FROM users WHERE id = ?");
    $p_stmt->bind_param("i", $patient_id);
    $p_stmt->execute();
    $p_result = $p_stmt->get_result();
    $patient = $p_result->fetch_assoc();
    $patient_name = $patient['full_name'] ?? 'A patient';
    $p_stmt->close();

    // Create notification for counselor
    $title = "New Appointment Request";
    $message = "$patient_name has requested a session for $date at $time.";
    $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Appointment')");
    $notify->bind_param("iss", $counselor_id, $title, $message);
    $notify->execute();
    $notify->close();

    echo json_encode(["status" => "success", "appointment_id" => $appointment_id, "message" => "Appointment booked successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
