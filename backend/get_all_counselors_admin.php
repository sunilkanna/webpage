<?php
include 'db_connect.php';

$sql = "SELECT cq.user_id, cq.*, u.full_name, u.email 
        FROM counselor_qualifications cq 
        JOIN users u ON cq.user_id = u.id
        GROUP BY cq.user_id
        ORDER BY cq.id DESC"; 
        // No WHERE clause to filter by status, so it fetches All

$result = $conn->query($sql);

$all_counselors = [];
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $all_counselors[] = $row;
    }
}

echo json_encode(["status" => "success", "all_counselors" => $all_counselors]);

$conn->close();
?>
