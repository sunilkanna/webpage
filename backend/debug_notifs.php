<?php
include 'db_connect.php';

echo "<h2>Notification Table Debug</h2>";

$res = $conn->query("SELECT * FROM notifications ORDER BY created_at DESC LIMIT 20");
echo "<table border='1'><tr><th>ID</th><th>User ID</th><th>Title</th><th>Message</th><th>Type</th><th>Read</th><th>Date</th></tr>";
while ($row = $res->fetch_assoc()) {
    echo "<tr>";
    echo "<td>".$row['id']."</td>";
    echo "<td>".$row['user_id']."</td>";
    echo "<td>".$row['title']."</td>";
    echo "<td>".$row['message']."</td>";
    echo "<td>".$row['type']."</td>";
    echo "<td>".$row['is_read']."</td>";
    echo "<td>".$row['created_at']."</td>";
    echo "</tr>";
}
echo "</table>";

$res2 = $conn->query("SELECT u.id, u.full_name, u.user_type FROM users u ORDER BY u.id DESC LIMIT 5");
echo "<h2>Recent Users</h2>";
echo "<table border='1'><tr><th>ID</th><th>Name</th><th>User Type</th></tr>";
while ($row = $res2->fetch_assoc()) {
    echo "<tr>";
    echo "<td>".$row['id']."</td>";
    echo "<td>".$row['full_name']."</td>";
    echo "<td>".$row['user_type']."</td>";
    echo "</tr>";
}
echo "</table>";

$conn->close();
?>
