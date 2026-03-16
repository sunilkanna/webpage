<?php
include 'db_connect.php';

// Targeted fix for appointment 82
$url = "http://172.20.10.2/genecare/view_report.php?file=1773115821_49b5c3d2_upload_1773115820797.pdf";
// Wait, I need to check the actual file name on disk for that report.
// From debug_patient_82.php: "file_url":"...view_report.php?file=upload_1773115820797.pdf"
// But upload_report.php now adds time prefixes.
// Let's check the uploads directory.

$matches = glob("uploads/*_upload_1773115820797.pdf");
if (!empty($matches)) {
    $real_name = basename($matches[0]);
    $real_url = "http://172.20.10.2/genecare/view_report.php?file=" . urlencode($real_name);
    
    $conn->query("UPDATE appointments SET medical_report_url = '$real_url' WHERE id = 82");
    echo "Fixed appointment 82 with url: $real_url<br>";
} else {
    // If no prefix, use the one from patient_reports if it's already there
    $res = $conn->query("SELECT file_url FROM patient_reports WHERE patient_id = 1019 AND file_name LIKE '%upload_1773115820797.pdf%'");
    if ($res && $row = $res->fetch_assoc()) {
        $real_url = $row['file_url'];
        $conn->query("UPDATE appointments SET medical_report_url = '$real_url' WHERE id = 82");
        echo "Fixed appointment 82 using patient_reports record: $real_url<br>";
    } else {
        echo "Could not find file for appointment 82<br>";
    }
}

$conn->close();
?>
