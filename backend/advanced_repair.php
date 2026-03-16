<?php
include 'db_connect.php';
header('Content-Type: text/html');

echo "<h1>Advanced Report Repair</h1>";

// 1. First, fix ANY direct URLs in patient_reports that were broken by the previous bug
$res = $conn->query("SELECT id, file_url FROM patient_reports");
while ($row = $res->fetch_assoc()) {
    $url = $row['file_url'];
    if (strpos($url, 'view_report.php?file=') !== false) {
        $parts = explode('view_report.php?file=', $url);
        $file_name = urldecode(end($parts));
        
        if (!file_exists("uploads/" . $file_name)) {
            $matches = glob("uploads/*_" . $file_name);
            if (!empty($matches)) {
                $actual_file = basename($matches[0]);
                $new_url = $parts[0] . "view_report.php?file=" . urlencode($actual_file);
                $conn->query("UPDATE patient_reports SET file_url = '$new_url' WHERE id = " . $row['id']);
                echo "Fixed patient_report ID " . $row['id'] . ": $file_name -> $actual_file<br>";
            }
        }
    }
}

// 2. Now try to help appointments with content:// URIs
$res = $conn->query("SELECT id, patient_id, medical_report_url FROM appointments WHERE medical_report_url LIKE 'content://%'");
$fixed_count = 0;
while ($row = $res->fetch_assoc()) {
    $appointment_id = $row['id'];
    $patient_id = $row['patient_id'];
    
    $report_res = $conn->query("SELECT file_url FROM patient_reports WHERE patient_id = $patient_id ORDER BY id DESC LIMIT 1");
    if ($report_res && $report_res->num_rows > 0) {
        $report_row = $report_res->fetch_assoc();
        $server_url = $report_row['file_url'];
        
        $stmt = $conn->prepare("UPDATE appointments SET medical_report_url = ? WHERE id = ?");
        $stmt->bind_param("si", $server_url, $appointment_id);
        $stmt->execute();
        
        echo "Auto-aligned Appointment $appointment_id (Patient $patient_id) to latest report: " . htmlspecialchars($server_url) . "<br>";
        $fixed_count++;
    }
}

// 3. Fix malformed URLs in appointments
$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url LIKE '%view_report.php?file=%'");
while ($row = $res->fetch_assoc()) {
    $url = $row['medical_report_url'];
    $parts = explode('view_report.php?file=', $url);
    $file_name = urldecode(end($parts));
    
    if (!file_exists("uploads/" . $file_name)) {
        $matches = glob("uploads/*_" . $file_name);
        if (!empty($matches)) {
            $actual_file = basename($matches[0]);
            $new_url = $parts[0] . "view_report.php?file=" . urlencode($actual_file);
            $conn->query("UPDATE appointments SET medical_report_url = '$new_url' WHERE id = " . $row['id']);
            echo "Fixed appointment ID " . $row['id'] . ": $file_name -> $actual_file<br>";
        }
    }
}

echo "<h2>Repair Complete. Fixed $fixed_count content:// URIs.</h2>";
$conn->close();
?>
