<?php
include 'db_connect.php';

$sql = "UPDATE counselor_qualifications SET certificate_url = '' WHERE certificate_url LIKE '%example.com%'";
if ($conn->query($sql) === TRUE) {
    echo "Dummy certificates cleared successfully. Affected rows: " . $conn->affected_rows . "\n";
} else {
    echo "Error updating record: " . $conn->error . "\n";
}

$conn->close();
?>
