<?php
include 'db_connect.php';
include 'add_system_log.php';

$data = json_decode(file_get_contents("php://input"), true);
$user_id = $data['user_id'] ?? null;
$action = $data['action'] ?? null; // delete, suspend, activate

if (!$user_id || !$action) {
    echo json_encode(["status" => "error", "message" => "User ID and action required"]);
    exit();
}

if ($action == 'delete') {
    $conn->begin_transaction();
    try {
        // Clean up related records (those without ON DELETE CASCADE)
        
        // 1. Appointments (depends on users as patient_id/counselor_id)
        $conn->query("DELETE FROM payments WHERE appointment_id IN (SELECT id FROM appointments WHERE patient_id = $user_id OR counselor_id = $user_id)");
        $conn->query("DELETE FROM feedback WHERE appointment_id IN (SELECT id FROM appointments WHERE patient_id = $user_id OR counselor_id = $user_id)");
        $conn->query("DELETE FROM appointments WHERE patient_id = $user_id OR counselor_id = $user_id");
        
        // 2. Messages
        $conn->query("DELETE FROM messages WHERE sender_id = $user_id OR receiver_id = $user_id");
        
        // 3. Reports & Assessments
        $conn->query("DELETE FROM patient_reports WHERE patient_id = $user_id");
        $conn->query("DELETE FROM risk_assessments WHERE patient_id = $user_id");
        
        // 4. Finally delete the user (cascades to profiles and medical history)
        $stmt = $conn->prepare("DELETE FROM users WHERE id = ?");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        
        add_system_log($conn, "User ID $user_id and all related data were deleted", 'WARNING', 'Admin');
        $conn->commit();
        echo json_encode(["status" => "success", "message" => "User and related data deleted"]);
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
} else {
    // Handle suspend/activate (would need a status column in users)
    echo json_encode(["status" => "error", "message" => "Action not implemented"]);
}

$conn->close();
?>
