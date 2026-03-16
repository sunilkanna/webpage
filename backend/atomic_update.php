<?php
include 'db_connect.php';
header('Content-Type: text/plain');

$id = 80;
$new_url = "http://172.20.10.2/genecare/view_report.php?file=1773115823_18ca0e77_upload_1773115820797.pdf";

echo "Updating ID $id to $new_url\n";
$stmt = $conn->prepare("UPDATE appointments SET medical_report_url = ? WHERE id = ?");
$stmt->bind_param("si", $new_url, $id);
$stmt->execute();
echo "Affected rows: " . $stmt->affected_rows . "\n";

$res = $conn->query("SELECT medical_report_url FROM appointments WHERE id = $id");
$row = $res->fetch_assoc();
echo "Current URL in DB: " . $row['medical_report_url'] . "\n";

$conn->close();
?>
