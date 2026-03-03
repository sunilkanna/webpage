<?php
include 'db_connect.php';

$sql = "SELECT DISTINCT cq.user_id, cq.*, u.full_name, u.email 
        FROM counselor_qualifications cq 
        JOIN users u ON cq.user_id = u.id 
        WHERE cq.status = 'Pending'
        GROUP BY cq.user_id
        ORDER BY cq.id DESC";

$result = $conn->query($sql);

$pending = [];
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $pending[] = $row;
    }
}

echo json_encode(["status" => "success", "pending_counselors" => $pending]);

$conn->close();
?>
