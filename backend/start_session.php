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

    // Time validation: Cannot join more than 5 minutes early
    $appt_date = $appointment['appointment_date'];
    $appt_time_str = $appointment['time_slot'];
    
    $appt_datetime = DateTime::createFromFormat('Y-m-d h:i A', "$appt_date $appt_time_str");
    if (!$appt_datetime) {
        $appt_datetime = DateTime::createFromFormat('Y-m-d g:i A', "$appt_date $appt_time_str");
    }

    if ($appt_datetime) {
        $now = new DateTime();
        $diff_seconds = $appt_datetime->getTimestamp() - $now->getTimestamp();
        
        // Allow joining up to 5 minutes (300 seconds) early
        if ($diff_seconds > 300) {
            echo json_encode([
                "status" => "too_early",
                "message" => "It's too early to join this session.",
                "seconds_until_start" => $diff_seconds,
                "appointment_time" => $appt_time_str,
                "appointment_date" => $appt_date
            ]);
            exit();
        }
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
        
        
        $meeting_link = WEBSITE_URL . "/video-call/" . $appointment_id;

        if (empty($appointment['meeting_link']) || strpos($appointment['meeting_link'], WEBSITE_URL) === false) {
            $fix = $conn->prepare("UPDATE appointments SET meeting_link = ? WHERE id = ?");
            $fix->bind_param("si", $meeting_link, $appointment_id);
            $fix->execute();
            $fix->close();
        }
    } else {
        $jwt_error = "JaaS not configured, using public fallback";
        if (empty($appointment['meeting_link']) || strpos($appointment['meeting_link'], WEBSITE_URL) === false) {
            $meeting_link = WEBSITE_URL . "/video-call/" . $appointment_id;
            $fix = $conn->prepare("UPDATE appointments SET meeting_link = ? WHERE id = ?");
            $fix->bind_param("si", $meeting_link, $appointment_id);
            $fix->execute();
            $fix->close();
        } else {
            $meeting_link = $appointment['meeting_link'];
        }
    }

    // Fetch patient reports
    $report_stmt = $conn->prepare("SELECT * FROM patient_reports WHERE patient_id = ? ORDER BY uploaded_at DESC");
    $patient_id = $appointment['patient_id'];
    $report_stmt->bind_param("i", $patient_id);
    $report_stmt->execute();
    $report_result = $report_stmt->get_result();
    $patient_reports = [];
    while ($row = $report_result->fetch_assoc()) {
        $patient_reports[] = [
            "file_name" => $row['file_name'] ?? 'Report',
            "file_url" => $row['file_url'],
            "uploaded_at" => $row['uploaded_at']
        ];
    }
    $report_stmt->close();

    $jitsi_direct_link = "https://8x8.vc/" . JAAS_APP_ID . "/" . $room_name;

    echo json_encode([
        "status" => "success",
        "message" => "Session started",
        "meeting_link" => $meeting_link,
        "jitsi_direct_link" => $jitsi_direct_link,
        "jwt" => $jwt,
        "jwt_error" => $jwt_error,
        "is_moderator" => $is_counselor,
        "jaas_app_id" => defined('JAAS_APP_ID') ? JAAS_APP_ID : null,
        "patient_name" => $appointment['patient_name'],
        "counselor_name" => $appointment['counselor_name'],
        "appointment_date" => $appointment['appointment_date'],
        "time_slot" => $appointment['time_slot'],
        "medical_report_url" => $appointment['medical_report_url'],
        "patient_reports" => $patient_reports
    ]);

} catch (Throwable $e) {
    echo json_encode(["status" => "error", "message" => "Server Error: " . $e->getMessage()]);
}

$conn->close();
?>
