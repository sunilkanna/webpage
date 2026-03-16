<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);
$appointment_id = $data['appointment_id'] ?? $_GET['appointment_id'] ?? null;

if (!$appointment_id) {
    echo json_encode(["status" => "error", "message" => "Appointment ID required"]);
    exit();
}


// Fetch appointment details along with patient and counselor info, including session fields
$sql = "SELECT a.id, a.patient_id, a.counselor_id, a.appointment_date, a.time_slot, 
        a.status, a.meeting_link, a.session_start_time, a.session_end_time, 
        a.session_duration_minutes, a.created_at, a.medical_report_url,
        p.full_name as patient_name, 
        c.full_name as counselor_name,
        cp.consultation_fee
        FROM appointments a
        JOIN users p ON a.patient_id = p.id
        JOIN users c ON a.counselor_id = c.id
        LEFT JOIN counselor_profiles cp ON c.id = cp.user_id
        WHERE a.id = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $appointment_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode(["status" => "success", "appointment" => $row]);
} else {
    echo json_encode(["status" => "error", "message" => "Appointment not found"]);
}

$stmt->close();
$conn->close();
?>
