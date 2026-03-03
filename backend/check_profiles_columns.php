<?php
include 'db_connect.php';
$result = $conn->query("SHOW COLUMNS FROM counselor_profiles");
while($row = $result->fetch_assoc()) {
    echo $row['Field'] . "\n";
}
?>
