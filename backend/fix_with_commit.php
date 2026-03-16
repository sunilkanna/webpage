<?php
include 'db_connect.php';
$new_url = "http://172.20.10.2/genecare/view_report.php?file=1773115823_18ca0e77_upload_1773115820797.pdf";

$conn->autocommit(TRUE); // Ensure autocommit is on

foreach ([80, 81] as $id) {
    $stmt = $conn->prepare("UPDATE appointments SET medical_report_url = ? WHERE id = ?");
    $stmt->bind_param("si", $new_url, $id);
    $stmt->execute();
    echo "Fixed $id. Affected: " . $stmt->affected_rows . "<br>";
}

$conn->commit();
$conn->close();
?>
