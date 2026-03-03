<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);
$appointment_id = $data['appointment_id'] ?? null;

if (!$appointment_id) {
    echo json_encode(["status" => "error", "message" => "Appointment ID required"]);
    exit();
}

// Fetch appointment with session info
$stmt = $conn->prepare("SELECT a.*, cp.consultation_fee, 
    p.full_name as patient_name, c.full_name as counselor_name
    FROM appointments a
    JOIN users p ON a.patient_id = p.id
    JOIN users c ON a.counselor_id = c.id
    LEFT JOIN counselor_profiles cp ON c.id = cp.user_id
    WHERE a.id = ?");
$stmt->bind_param("i", $appointment_id);
$stmt->execute();
$result = $stmt->get_result();
$appointment = $result->fetch_assoc();
$stmt->close();

if (!$appointment) {
    echo json_encode(["status" => "error", "message" => "Appointment not found"]);
    exit();
}

// Calculate duration
$duration_minutes = 0;
if (!empty($appointment['session_start_time'])) {
    $start = new DateTime($appointment['session_start_time']);
    $end = new DateTime(); // now
    $diff = $start->diff($end);
    $duration_minutes = ($diff->h * 60) + $diff->i;
    if ($duration_minutes < 1) $duration_minutes = 1; // minimum 1 minute
}

$fee = $appointment['consultation_fee'] ?? 0;

// Update appointment: set end time, duration, and mark as completed
$conn->begin_transaction();

try {
    // Update appointment
    $update = $conn->prepare("UPDATE appointments SET status = 'Completed', session_end_time = NOW(), session_duration_minutes = ? WHERE id = ?");
    $update->bind_param("ii", $duration_minutes, $appointment_id);
    $update->execute();
    $update->close();

    // Create payment record
    $payment = $conn->prepare("INSERT INTO payments (appointment_id, amount, payment_method, status) VALUES (?, ?, 'Pending', 'Pending')");
    $payment->bind_param("id", $appointment_id, $fee);
    $payment->execute();
    $payment->close();

    // Notify patient that session is complete
    $notify_msg = "Your session with Dr. " . $appointment['counselor_name'] . " has been completed. Duration: " . $duration_minutes . " minutes.";
    $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, 'Session Completed', ?, 'Session')");
    $notify->bind_param("is", $appointment['patient_id'], $notify_msg);
    $notify->execute();
    $notify->close();

    $conn->commit();

    echo json_encode([
        "status" => "success",
        "message" => "Session ended successfully",
        "session_duration_minutes" => $duration_minutes,
        "consultation_fee" => $fee,
        "patient_name" => $appointment['patient_name'],
        "counselor_name" => $appointment['counselor_name']
    ]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => "Error ending session: " . $e->getMessage()]);
}

$conn->close();
?>
