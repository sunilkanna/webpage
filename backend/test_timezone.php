<?php
include 'db_connect.php';

echo "PHP Time (Asia/Kolkata): " . date('Y-m-d H:i:s') . "\n";

$result = $conn->query("SELECT NOW() as mysql_time");
$row = $result->fetch_assoc();
echo "MySQL Time (+05:30): " . $row['mysql_time'] . "\n";

if (date('Y-m-d H:i:s') === $row['mysql_time'] || abs(strtotime(date('Y-m-d H:i:s')) - strtotime($row['mysql_time'])) < 2) {
    echo "SUCCESS: Timezones are synchronized.\n";
} else {
    echo "FAILURE: Timezones are NOT synchronized.\n";
}
?>
