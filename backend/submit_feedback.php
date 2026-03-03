<?php
include 'db_connect.php';

// Handle JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

$appointment_id = $_POST['appointment_id'] ?? $data['appointment_id'] ?? null;
$patient_id = $_POST['patient_id'] ?? $data['patient_id'] ?? null;
$rating = $_POST['rating'] ?? $data['rating'] ?? null;
$comments = $_POST['comments'] ?? $data['comments'] ?? null;

if (empty($appointment_id) || empty($patient_id) || $rating === null) {
    echo json_encode(["status" => "error", "message" => "Required fields missing"]);
    exit();
}

$stmt = $conn->prepare("INSERT INTO feedback (appointment_id, patient_id, rating, comments) VALUES (?, ?, ?, ?)");
$stmt->bind_param("iiis", $appointment_id, $patient_id, $rating, $comments);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Feedback submitted"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
