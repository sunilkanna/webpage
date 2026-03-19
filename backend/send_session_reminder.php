<?php
/**
 * Session Reminder Script
 * 
 * Run this script every minute via Windows Task Scheduler:
 * schtasks /create /tn "GeneCareReminder" /tr "C:\xampp\php\php.exe C:\xampp\htdocs\genecare\send_session_reminder.php" /sc MINUTE /mo 1 /f
 *
 * Or call it via browser: http://localhost/genecare/send_session_reminder.php
 */
include 'db_connect.php';

date_default_timezone_set('Asia/Kolkata');

$now = new DateTime();
$results = [];

// Get all confirmed appointments that haven't had a reminder sent
$stmt = $conn->prepare("
    SELECT a.id, a.patient_id, a.counselor_id, a.appointment_date, a.time_slot, a.meeting_link,
           p.full_name as patient_name, p.email as patient_email,
           c.full_name as counselor_name
    FROM appointments a
    JOIN users p ON a.patient_id = p.id
    JOIN users c ON a.counselor_id = c.id
    WHERE a.status = 'Confirmed' AND a.reminder_sent = 0
");
$stmt->execute();
$result = $stmt->get_result();
$appointments = $result->fetch_all(MYSQLI_ASSOC);
$stmt->close();

foreach ($appointments as $appt) {
    // Combine appointment_date + time_slot into a DateTime
    // time_slot format: "10:00 AM", "02:30 PM", etc.
    $date_str = $appt['appointment_date'];
    $time_str = $appt['time_slot'];

    // Parse the combined datetime
    $session_time = DateTime::createFromFormat('Y-m-d h:i A', "$date_str $time_str");
    if (!$session_time) {
        // Try alternate format without leading zero
        $session_time = DateTime::createFromFormat('Y-m-d g:i A', "$date_str $time_str");
    }
    if (!$session_time) {
        $results[] = ["appointment_id" => $appt['id'], "status" => "skipped", "reason" => "Could not parse time: $date_str $time_str"];
        continue;
    }

    // Calculate minutes until session
    $diff = $now->diff($session_time);
    $minutes_until = ($session_time->getTimestamp() - $now->getTimestamp()) / 60;

    // Send reminder if session is between 0 and 10 minutes away (inclusive)
    if ($minutes_until > 0 && $minutes_until <= 10) {
        $counselor_name = $appt['counselor_name'];
        $patient_name = $appt['patient_name'];
        $patient_email = $appt['patient_email'];
        $appointment_id = $appt['id'];

        // 1. Send in-app notification
        $title = "Session Starting Soon! ⏰";
        $message = "Your session with Dr. $counselor_name starts in " . ceil($minutes_until) . " minutes. Please get ready!";
        $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Reminder')");
        $notify->bind_param("iss", $appt['patient_id'], $title, $message);
        $notify->execute();
        $notify->close();

        // 2. Send email notification
        $email_sent = false;
        $email_error = null;
        if (!empty($patient_email)) {
            try {
                $p_email = escapeshellarg($patient_email);
                $p_name = escapeshellarg($patient_name);
                $c_name = escapeshellarg($counselor_name);
                $a_date = escapeshellarg($date_str);
                $a_time = escapeshellarg($time_str);
                $m_link = escapeshellarg($appt['meeting_link'] ?? '');

                $script_path = __DIR__ . DIRECTORY_SEPARATOR . "send_reminder_email.py";
                // Execute in background
                exec("start /B python \"$script_path\" $p_email $p_name $c_name $a_date $a_time $m_link > NUL 2>&1");
                $email_sent = true; 
            } catch (Exception $e) {
                $email_error = $e->getMessage();
            }
        }

        // 3. Mark reminder as sent
        $update = $conn->prepare("UPDATE appointments SET reminder_sent = 1 WHERE id = ?");
        $update->bind_param("i", $appointment_id);
        $update->execute();
        $update->close();

        $results[] = [
            "appointment_id" => $appointment_id,
            "patient" => $patient_name,
            "status" => "reminder_sent",
            "email_sent" => $email_sent,
            "email_error" => $email_error
        ];
    }
}

$conn->close();

echo json_encode([
    "status" => "success",
    "timestamp" => $now->format('Y-m-d H:i:s'),
    "reminders_processed" => count($results),
    "details" => $results
], JSON_PRETTY_PRINT);
?>
