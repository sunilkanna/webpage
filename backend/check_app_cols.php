<?php
include 'db_connect.php';
$res = $conn->query("DESCRIBE appointments");
$cols = [];
while($row = $res->fetch_assoc()) $cols[] = $row['Field'];
echo "COLUMNS: " . implode(", ", $cols);
?>
