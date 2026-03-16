<?php
include 'db_connect_v2.php';
echo "DB_CONNECTED_OK\n";
$res = $conn->query("DESCRIBE appointments");
while($row = $res->fetch_assoc()) echo $row['Field'] . " ";
?>
