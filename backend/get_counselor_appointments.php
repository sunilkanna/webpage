<?php
ob_start();
header('Content-Type: application/json');
include_once 'db_connect.php';

$counselor_id = $_GET['counselor_id'] ?? null;

if (!$counselor_id) {
    if (ob_get_length()) ob_clean();
    echo json_encode(["status" => "error", "message" => "Counselor ID required"]);
    exit();
}

// Enable strict reporting to catch errors in try-catch
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    // Fetch appointments for this counselor
    $sql = "SELECT DISTINCT a.id, a.appointment_date, a.time_slot, a.status, a.meeting_link, a.medical_report_url,
                   u.full_name as patient_name, u.email as patient_email, pp.profile_image_url as patient_image,
                   'Initial Consultation' as type,
                   'Routine Checkup' as reason,
                   (SELECT COUNT(*) FROM patient_reports pr WHERE pr.patient_id = a.patient_id) as patient_report_count,
                   (SELECT file_url FROM patient_reports pr WHERE pr.patient_id = a.patient_id ORDER BY uploaded_at DESC LIMIT 1) as latest_patient_report_url
            FROM appointments a 
            JOIN users u ON a.patient_id = u.id 
            LEFT JOIN patient_profiles pp ON a.patient_id = pp.user_id
            WHERE a.counselor_id = ?
            GROUP BY a.id, a.appointment_date, a.time_slot
            ORDER BY a.appointment_date DESC, a.time_slot ASC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $counselor_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $appointments = [];
    while ($row = $result->fetch_assoc()) {
        $row['image_initial'] = !empty($row['patient_name']) ? substr($row['patient_name'], 0, 1) : "P";
        $row['image_color_hex'] = '#FFCC80'; 
        
        // Prioritize the report attached to the appointment
        if (!empty($row['medical_report_url'])) {
            $row['report_url'] = $row['medical_report_url'];
            $row['has_report'] = true;
        } else {
            // Fallback to the latest report from patient_reports
            $row['report_url'] = $row['latest_patient_report_url'];
            $row['has_report'] = ($row['patient_report_count'] > 0);
        }
        
        // Clean up internal fields
        unset($row['medical_report_url']);
        unset($row['patient_report_count']);
        unset($row['latest_patient_report_url']);
        
        $appointments[] = $row;
    }

    if (ob_get_length()) ob_clean();
    echo json_encode(["status" => "success", "appointments" => $appointments]);

    $stmt->close();
} catch (Throwable $e) {
    if (ob_get_length()) ob_clean();
    echo json_encode(["status" => "error", "message" => "Server Error: " . $e->getMessage()]);
}

$conn->close();
ob_end_flush();
?>
