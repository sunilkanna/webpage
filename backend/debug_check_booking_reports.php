<?php
include 'db_connect.php';
$res = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url IS NOT NULL AND medical_report_url != ''");
echo "<h2>Booking Reports URLs:</h2>";
while($row = $res->fetch_assoc()) {
    echo "Appt ID: " . $row['id'] . " -> URL: " . htmlspecialchars($row['medical_report_url']) . "<br>";
}
$res2 = $conn->query("SELECT id, file_url FROM counselor_reports");
echo "<h2>Counselor Reports URLs:</h2>";
while($row2 = $res2->fetch_assoc()) {
    echo "ID: " . $row2['id'] . " -> URL: " . htmlspecialchars($row2['file_url']) . "<br>";
}
?>
