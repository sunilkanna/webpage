<?php
include 'db_connect.php';

// 1. Get Patient Reports
$patient_sql = "SELECT pr.*, u.full_name as patient_name FROM patient_reports pr JOIN users u ON pr.patient_id = u.id ORDER BY pr.uploaded_at DESC";
$p_result = $conn->query($patient_sql);
$patient_reports = [];
if ($p_result) {
    while($row = $p_result->fetch_assoc()) {
        $row['title'] = $row['file_name'];
        $row['type'] = 'Patient Upload';
        $row['date'] = $row['uploaded_at'];
        $patient_reports[] = $row;
    }
}

// 2. Get Counselor Reports (Generated Summaries)
$counselor_sql = "SELECT cr.*, u.full_name as counselor_name FROM counselor_reports cr JOIN users u ON cr.counselor_id = u.id ORDER BY cr.created_at DESC";
$c_result = $conn->query($counselor_sql);
$counselor_reports = [];
if ($c_result) {
    while($row = $c_result->fetch_assoc()) {
        $row['title'] = "Session Report - " . $row['counselor_name'];
        $row['type'] = 'Counselor Generated';
        $row['date'] = $row['created_at'];
        $counselor_reports[] = $row;
    }
}

// 3. Get Counselor Certifications (Doctor's Certification)
$cert_sql = "SELECT cq.id, cq.user_id, cq.certificate_url, u.full_name as counselor_name, cq.submitted_at as date 
             FROM counselor_qualifications cq 
             JOIN users u ON cq.user_id = u.id 
             WHERE cq.certificate_url IS NOT NULL AND cq.certificate_url != ''
             ORDER BY cq.submitted_at DESC";
$cert_result = $conn->query($cert_sql);
$certifications = [];
if ($cert_result) {
    while($row = $cert_result->fetch_assoc()) {
        $row['title'] = "Certification - " . $row['counselor_name'];
        $row['type'] = 'Doctor Certification';
        $row['file_url'] = $row['certificate_url'];
        $certifications[] = $row;
    }
}

echo json_encode([
    "status" => "success", 
    "reports" => array_merge($patient_reports, $counselor_reports, $certifications)
]);

$conn->close();
?>
