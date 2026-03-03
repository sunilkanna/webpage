<?php
include 'db_connect.php';

$sql = "SELECT DISTINCT id, full_name as name, email, user_type as role, 'Active' as status FROM users";
// Note: 'status' column doesn't exist in 'users' yet. I should add it if I want suspension.
// For now, I'll default to Active.

$result = $conn->query($sql);
$users = [];
while($row = $result->fetch_assoc()) {
    $users[] = $row;
}

echo json_encode(["status" => "success", "users" => $users]);
$conn->close();
?>
