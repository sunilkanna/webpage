<?php
include 'db_connect.php';

$sql = "SELECT id, user_id, doctor_name, certificate_url FROM counselor_qualifications";
$result = $conn->query($sql);

echo "--- Counselor Qualifications ---\n";
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        echo "ID: " . $row["id"] . " | UserID: " . $row["user_id"] . " | Name: " . $row["doctor_name"] . " | URL: " . $row["certificate_url"] . "\n";
    }
} else {
    echo "No records found.\n";
}

$conn->close();
?>
