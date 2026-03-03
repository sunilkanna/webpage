<?php
include 'db_connect.php';
$res = $conn->query("SELECT is_read FROM messages LIMIT 1");
$row = $res->fetch_assoc();
echo "ROW: ";
print_r($row);
echo "\nJSON: ";
echo json_encode($row);
?>
