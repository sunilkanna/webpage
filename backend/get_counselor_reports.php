<?php
include 'db_connect.php';

$counselor_id = $_GET['counselor_id'] ?? null;

if (!$counselor_id) {
    echo json_encode(["status" => "error", "message" => "Counselor ID required"]);
    exit();
}

$stmt = $conn->prepare("
    (SELECT 
        a.id as id, 
        'Booking Report' as title, 
        'Medical' as category, 
        a.appointment_date as date, 
        u.full_name as patientName, 
        a.patient_id as patientId, 
        a.medical_report_url as fileUrl
    FROM appointments a
    JOIN users u ON a.patient_id = u.id
    WHERE a.counselor_id = ? AND a.medical_report_url IS NOT NULL AND a.medical_report_url != '')
    UNION ALL
    (SELECT 
        c.id as id, 
        c.title as title, 
        c.category as category, 
        c.report_date as date, 
        'System' as patientName, 
        0 as patientId, 
        c.file_url as fileUrl
    FROM counselor_reports c
    WHERE c.counselor_id = ?)
    ORDER BY date DESC
");
$stmt->bind_param("ii", $counselor_id, $counselor_id);
$stmt->execute();
$result = $stmt->get_result();

$reports = [];
while ($row = $result->fetch_assoc()) {
    $reports[] = $row;
}

echo json_encode(["status" => "success", "reports" => $reports]);

$stmt->close();
$conn->close();
?>
