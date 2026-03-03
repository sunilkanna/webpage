<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);
$appointment_id = $data['appointment_id'] ?? null;
$user_id = $data['user_id'] ?? null;

if (!$appointment_id || !$user_id) {
    echo json_encode(["status" => "error", "message" => "Appointment ID and User ID required"]);
    exit();
}

// Enable strict reporting
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    // Fetch appointment details
    $stmt = $conn->prepare("SELECT a.*, 
        p.full_name as patient_name, 
        c.full_name as counselor_name,
        cp.consultation_fee
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
        throw new Exception("Appointment not found (ID: $appointment_id)");
    }

    // Validate the user is either the patient or counselor
    if ($appointment['patient_id'] != $user_id && $appointment['counselor_id'] != $user_id) {
        throw new Exception("Unauthorized: user $user_id is not part of this appointment");
    }

    // Validate appointment is Confirmed
    if ($appointment['status'] !== 'Confirmed') {
        throw new Exception("Session not confirmed yet. Current status: " . $appointment['status']);
    }

    // Set session_start_time if not already set (first joiner triggers it)
    if (empty($appointment['session_start_time'])) {
        $update = $conn->prepare("UPDATE appointments SET session_start_time = NOW() WHERE id = ?");
        $update->bind_param("i", $appointment_id);
        $update->execute();
        $update->close();
    }

    $is_counselor = ($appointment['counselor_id'] == $user_id);
    $user_name = $is_counselor ? $appointment['counselor_name'] : $appointment['patient_name'];
    $room_name = "genecare-room-" . $appointment_id;

    // Include JaaS Token Logic
    include_once 'jaas_config.php';
    include_once 'jwt_helper.php';

    // Generate JWT for JaaS
    $jwt = null;
    $jwt_error = null;
    if (defined('JAAS_APP_ID') && strpos(JAAS_APP_ID, 'REPLACE_ME') === false) {
        $jwt = generateJitsiJwt(
            JAAS_APP_ID,
            JAAS_KEY_ID,
            JAAS_PRIVATE_KEY,
            $room_name,
            $user_name,
            $is_counselor,
            $user_id
        );
        
        if (!$jwt) {
            $jwt_error = "Failed to generate JWT. Check OpenSSL and keys.";
        }
        
        $meeting_link = "https://8x8.vc/" . JAAS_APP_ID . "/" . $room_name;

        if (empty($appointment['meeting_link']) || strpos($appointment['meeting_link'], '8x8.vc') === false) {
            $fix = $conn->prepare("UPDATE appointments SET meeting_link = ? WHERE id = ?");
            $fix->bind_param("si", $meeting_link, $appointment_id);
            $fix->execute();
            $fix->close();
        }
    } else {
        $jwt_error = "JaaS not configured, using public fallback";
        if (empty($appointment['meeting_link'])) {
            $meeting_link = "https://meet.jit.si/" . $room_name;
            $fix = $conn->prepare("UPDATE appointments SET meeting_link = ? WHERE id = ?");
            $fix->bind_param("si", $meeting_link, $appointment_id);
            $fix->execute();
            $fix->close();
        } else {
            $meeting_link = $appointment['meeting_link'];
        }
    }

    echo json_encode([
        "status" => "success",
        "message" => "Session started",
        "meeting_link" => $meeting_link,
        "jwt" => $jwt,
        "jwt_error" => $jwt_error,
        "is_moderator" => $is_counselor,
        "patient_name" => $appointment['patient_name'],
        "counselor_name" => $appointment['counselor_name'],
        "appointment_date" => $appointment['appointment_date'],
        "time_slot" => $appointment['time_slot'],
        "medical_report_url" => $appointment['medical_report_url']
    ]);

} catch (Throwable $e) {
    echo json_encode(["status" => "error", "message" => "Server Error: " . $e->getMessage()]);
}

$conn->close();
?>
