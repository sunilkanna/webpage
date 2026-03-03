<?php
include 'db_connect.php';

echo "Table: appointments\n";
$result = $conn->query("DESCRIBE appointments");
while ($row = $result->fetch_assoc()) {
    print_r($row);
}

echo "\nRecent Bookings:\n";
$result = $conn->query("SELECT * FROM appointments ORDER BY id DESC LIMIT 5");
while ($row = $result->fetch_assoc()) {
    print_r($row);
}
?>
