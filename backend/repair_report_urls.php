<?php
include 'db_connect.php';

echo "<h1>Report URL Repair Script</h1>";

$new_ip = "172.20.10.2";

// 1. Repair patient_reports table
echo "<h2>1. Repairing patient_reports</h2>";
$res = $conn->query("SELECT id, file_url FROM patient_reports");
$count = 0;
while ($row = $res->fetch_assoc()) {
    $old_url = $row['file_url'];
    $new_url = $old_url;

    // Replace localhost/127.0.0.1
    $new_url = str_replace(['localhost', '127.0.0.1'], $new_ip, $new_url);

    // Convert direct uploads path to proxy path
    if (strpos($new_url, '/uploads/') !== false && strpos($new_url, 'view_report.php') === false) {
        $parts = explode('/uploads/', $new_url);
        $file_name = end($parts);
        $new_url = $parts[0] . "/view_report.php?file=" . urlencode($file_name);
    }

    if ($new_url !== $old_url) {
        $stmt = $conn->prepare("UPDATE patient_reports SET file_url = ? WHERE id = ?");
        $stmt->bind_param("si", $new_url, $row['id']);
        $stmt->execute();
        echo "Updated ID " . $row['id'] . ": " . htmlspecialchars($old_url) . " -> " . htmlspecialchars($new_url) . "<br>";
        $count++;
    }
}
echo "Total patient_reports updated: $count<br>";

// 2. Repair appointments table
echo "<h2>2. Repairing appointments</h2>";
$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url IS NOT NULL");
$count = 0;
while ($row = $res->fetch_assoc()) {
    $old_url = $row['medical_report_url'];
    $new_url = $old_url;

    // Ignore content:// URIs (cannot be repaired automatically)
    if (strpos($new_url, 'content://') === 0) continue;

    // Replace localhost/127.0.0.1
    $new_url = str_replace(['localhost', '127.0.0.1'], $new_ip, $new_url);

    // Convert direct uploads path to proxy path
    if (strpos($new_url, '/uploads/') !== false && strpos($new_url, 'view_report.php') === false) {
        $parts = explode('/uploads/', $new_url);
        $file_name = end($parts);
        $new_url = $parts[0] . "/view_report.php?file=" . urlencode($file_name);
    }

    if ($new_url !== $old_url) {
        $stmt = $conn->prepare("UPDATE appointments SET medical_report_url = ? WHERE id = ?");
        $stmt->bind_param("si", $new_url, $row['id']);
        $stmt->execute();
        echo "Updated ID " . $row['id'] . ": " . htmlspecialchars($old_url) . " -> " . htmlspecialchars($new_url) . "<br>";
        $count++;
    }
}
echo "Total appointments updated: $count<br>";

echo "<h2>Repair Complete</h2>";
$conn->close();
?>
