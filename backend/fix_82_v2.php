<?php
include 'db_connect.php';

$real_url = "http://172.20.10.2/genecare/view_report.php?file=1773115823_18ca0e77_upload_1773115820797.pdf";

$stmt = $conn->prepare("UPDATE appointments SET medical_report_url = ? WHERE id = 82");
$stmt->bind_param("s", $real_url);
if ($stmt->execute()) {
    echo "Successfully updated appointment 82 to $real_url. Rows affected: " . $stmt->affected_rows;
} else {
    echo "Error updating appointment 82: " . $stmt->error;
}
$stmt->close();
$conn->close();
?>
