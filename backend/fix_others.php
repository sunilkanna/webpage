<?php
include 'db_connect.php';

$real_url = "http://172.20.10.2/genecare/view_report.php?file=1773115823_18ca0e77_upload_1773115820797.pdf";

foreach ([80, 81] as $id) {
    if ($conn->query("UPDATE appointments SET medical_report_url = '$real_url' WHERE id = $id")) {
        echo "Fixed appointment $id<br>";
    } else {
        echo "Error fixing $id: " . $conn->error . "<br>";
    }
}

$conn->close();
?>
