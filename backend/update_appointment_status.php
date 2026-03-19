<?php
include 'db_connect.php';
// Email logic is now handled by send_confirmation_email.py

$data = json_decode(file_get_contents("php://input"), true);
$appointment_id = $_POST['appointment_id'] ?? $data['appointment_id'] ?? null;
$status = $_POST['status'] ?? $data['status'] ?? null; // 'Confirmed', 'Completed', 'Cancelled'
$rejection_reason = $_POST['rejection_reason'] ?? $data['rejection_reason'] ?? null;

// Require rejection reason when cancelling
if ($status === 'Cancelled' && empty($rejection_reason)) {
    echo json_encode(["status" => "error", "message" => "Rejection reason is required when cancelling an appointment"]);
    exit();
}

if (!$appointment_id || !$status) {
    echo json_encode(["status" => "error", "message" => "Appointment ID and status required"]);
    exit();
}

$conn->begin_transaction();

try {
    // If confirming, generate a meeting link
    $meeting_link = null;
    if ($status === 'Confirmed') {
        // Updated: Point to the GeneCare website session page instead of direct Jitsi
        $meeting_link = WEBSITE_URL . "/video-call/" . $appointment_id;
        
        $stmt = $conn->prepare("UPDATE appointments SET status = ?, meeting_link = ? WHERE id = ?");
        $stmt->bind_param("ssi", $status, $meeting_link, $appointment_id);
    } elseif ($status === 'Cancelled' && $rejection_reason) {
        $stmt = $conn->prepare("UPDATE appointments SET status = ?, rejection_reason = ? WHERE id = ?");
        $stmt->bind_param("ssi", $status, $rejection_reason, $appointment_id);
    } else {
        $stmt = $conn->prepare("UPDATE appointments SET status = ? WHERE id = ?");
        $stmt->bind_param("si", $status, $appointment_id);
    }
    
    $stmt->execute();
    $stmt->close();

    // Send notification to patient
    $appt = $conn->prepare("SELECT patient_id, counselor_id, appointment_date, time_slot FROM appointments WHERE id = ?");
    $appt->bind_param("i", $appointment_id);
    $appt->execute();
    $appt_result = $appt->get_result();
    $appt_data = $appt_result->fetch_assoc();
    $appt->close();

    if ($appt_data) {
        // Get counselor name
        $c_stmt = $conn->prepare("SELECT full_name FROM users WHERE id = ?");
        $c_stmt->bind_param("i", $appt_data['counselor_id']);
        $c_stmt->execute();
        $c_result = $c_stmt->get_result();
        $counselor = $c_result->fetch_assoc();
        $c_stmt->close();

        $counselor_name = $counselor['full_name'] ?? 'your counselor';

        if ($status === 'Confirmed') {
            $title = "Session Confirmed!";
            $message = "Your session with Dr. $counselor_name on " . $appt_data['appointment_date'] . " at " . $appt_data['time_slot'] . " has been confirmed.";
        } elseif ($status === 'Cancelled') {
            $title = "Session Cancelled";
            $message = "Your session with Dr. $counselor_name on " . $appt_data['appointment_date'] . " has been cancelled. Reason: " . $rejection_reason;
        } else {
            $title = "Session Update";
            $message = "Your session status has been updated to $status.";
        }

        $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Session')");
        $notify->bind_param("iss", $appt_data['patient_id'], $title, $message);
        $notify->execute();
        $notify->close();

        // Send email to patient when session is confirmed or cancelled
        $email_sent = false;
        $email_error = null;

        // Get patient email (needed for both confirmation and rejection emails)
        $pe_stmt = $conn->prepare("SELECT email, full_name FROM users WHERE id = ?");
        $pe_stmt->bind_param("i", $appt_data['patient_id']);
        $pe_stmt->execute();
        $pe_result = $pe_stmt->get_result();
        $patient_data = $pe_result->fetch_assoc();
        $pe_stmt->close();

        if ($patient_data && !empty($patient_data['email'])) {
            $patient_email = $patient_data['email'];
            $patient_name = $patient_data['full_name'] ?? 'Patient';
            $appt_date = $appt_data['appointment_date'];
            $appt_time = $appt_data['time_slot'];

            $p_email = escapeshellarg($patient_email);
            $p_name = escapeshellarg($patient_name);
            $c_name = escapeshellarg($counselor_name);
            $a_date = escapeshellarg($appt_date);
            $a_time = escapeshellarg($appt_time);

            try {
                if ($status === 'Confirmed' && $meeting_link) {
                    $m_link = escapeshellarg($meeting_link);
                    $script_path = __DIR__ . DIRECTORY_SEPARATOR . "send_confirmation_email.py";
                    
                    // Windows background execution: start /B ...
                    $command = "start /B python \"$script_path\" $p_email $p_name $c_name $a_date $a_time $m_link > NUL 2>&1";
                    pclose(popen($command, "r"));
                    $email_sent = true;
                } elseif ($status === 'Cancelled' && $rejection_reason) {
                    $r_reason = escapeshellarg($rejection_reason);
                    $script_path = __DIR__ . DIRECTORY_SEPARATOR . "send_rejection_email.py";
                    
                    // Windows background execution: start /B ...
                    $command = "start /B python \"$script_path\" $p_email $p_name $c_name $a_date $a_time $r_reason > NUL 2>&1";
                    pclose(popen($command, "r"));
                    $email_sent = true;
                }
            } catch (Exception $e) {
                $email_error = $e->getMessage();
            }
        }
    }

    $conn->commit();

    $response = ["status" => "success", "message" => "Appointment updated to $status"];
    if ($meeting_link) {
        $response["meeting_link"] = $meeting_link;
    }
    if ($email_sent) {
        $response["email_triggered"] = true;
    }
    if ($email_error) {
        $response["email_error"] = $email_error;
    }
    echo json_encode($response);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => "Error: " . $e->getMessage()]);
}

$conn->close();
?>
