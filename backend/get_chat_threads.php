<?php
include 'db_connect.php';

$counselor_id = $_GET['counselor_id'];

if (empty($counselor_id)) {
    echo json_encode(["status" => "error", "message" => "Counselor ID required"]);
    exit();
}

// Complex query to get latest message for each conversation
// This finds all unique users the counselor has interacted with
$sql = "
    SELECT 
        u.id AS user_id,
        u.full_name,
        u.profile_image_url, -- Assuming this column exists in users or joined from profiles
        m.message_text AS last_message,
        m.sent_at,
        (SELECT COUNT(*) FROM messages m2 WHERE m2.sender_id = u.id AND m2.receiver_id = ? AND m2.is_read = 0) AS unread_count
    FROM users u
    JOIN messages m ON (m.sender_id = u.id AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = u.id)
    WHERE u.id != ?
    AND m.sent_at = (
        SELECT MAX(m3.sent_at)
        FROM messages m3
        WHERE (m3.sender_id = u.id AND m3.receiver_id = ?) OR (m3.sender_id = ? AND m3.receiver_id = u.id)
    )
    ORDER BY m.sent_at DESC
";

// Note: profile_image_url might be in patient_profiles. Let's do a left join if needed, but for now assuming basic user info.
// Actually, let's adjust to join patient_profiles for image if user type is patient.

$sql = "
    SELECT 
        u.id AS user_id,
        u.full_name,
        p.profile_image_url,
        m.message_text AS last_message,
        m.sent_at AS time,
        (SELECT COUNT(*) FROM messages m2 WHERE m2.sender_id = u.id AND m2.receiver_id = ? AND m2.is_read = 0) AS unread_count
    FROM users u
    LEFT JOIN patient_profiles p ON u.id = p.user_id
    JOIN messages m ON m.id = (
        SELECT id FROM messages m3 
        WHERE (m3.sender_id = u.id AND m3.receiver_id = ?) OR (m3.sender_id = ? AND m3.receiver_id = u.id)
        ORDER BY m3.sent_at DESC LIMIT 1
    )
    WHERE (m.sender_id = ? OR m.receiver_id = ?) 
    AND u.id != ?
    AND EXISTS (
        SELECT 1 FROM appointments a 
        WHERE (a.patient_id = u.id AND a.counselor_id = ?) 
           OR (a.patient_id = ? AND a.counselor_id = u.id)
        AND a.status IN ('Confirmed', 'Pending', 'Completed')
    )
    GROUP BY u.id
    ORDER BY m.sent_at DESC
";

// Simplified approach used often: Get all unique other_ids, then fetch details. 
// But a single query is better. Let's stick to a robust one.

$stmt = $conn->prepare($sql);
// Params: 
// 1. receiver_id (for unread count) -> counselor_id
// 2. subquery m3 receiver -> counselor_id
// 3. subquery m3 sender -> counselor_id
// 4. main where: sender -> counselor_id
// 5. main where: receiver -> counselor_id
// 6. main where: u.id != counselor_id

$stmt->bind_param("iiiiiiii", $counselor_id, $counselor_id, $counselor_id, $counselor_id, $counselor_id, $counselor_id, $counselor_id, $counselor_id);
$stmt->execute();
$result = $stmt->get_result();

$threads = [];
while ($row = $result->fetch_assoc()) {
    // Determine avatar color hash if no image (mock logic for now or handle in app)
    $threads[] = [
        "id" => (string)$row['user_id'],
        "senderName" => $row['full_name'],
        "lastMessage" => $row['last_message'],
        "time" => $row['time'], // Client can format this
        "unreadCount" => (int)$row['unread_count'],
        "avatarUrl" => $row['profile_image_url']
    ];
}

echo json_encode(["status" => "success", "threads" => $threads]);

$stmt->close();
$conn->close();
?>
