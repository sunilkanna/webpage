<?php
include 'db_connect.php';

$table = 'counselor_qualifications';
$result = $conn->query("DESCRIBE $table");

if ($result) {
    echo "Schema for $table:\n";
    while ($row = $result->fetch_assoc()) {
        echo $row['Field'] . " - " . $row['Type'] . "\n";
    }
} else {
    echo "Error describing table: " . $conn->error;
}
$conn->close();
?>
