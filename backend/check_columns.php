<?php
include 'db_connect.php';
$result = $conn->query("SHOW COLUMNS FROM counselor_qualifications");
while($row = $result->fetch_assoc()) {
    echo $row['Field'] . "\n";
}
?>
