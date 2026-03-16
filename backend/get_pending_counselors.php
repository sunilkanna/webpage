<?php
include 'db_connect.php';

$sql = "SELECT cq.user_id, cq.*, u.full_name, u.email, cp.specialization, cp.experience_years, cp.consultation_fee
        FROM counselor_qualifications cq 
        JOIN users u ON cq.user_id = u.id 
        LEFT JOIN counselor_profiles cp ON cq.user_id = cp.user_id
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
