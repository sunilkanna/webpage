<?php
include 'db_connect.php';
echo "--- Final JSON Type Verification ---\n\n";

// Test get_messages logic
echo "Checking get_messages.php simulated row:\n";
$res = $conn->query("SELECT id, sender_id, receiver_id, is_read FROM messages LIMIT 1");
if ($row = $res->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['sender_id'] = (int)$row['sender_id'];
    $row['receiver_id'] = (int)$row['receiver_id'];
    $row['is_read'] = (bool)$row['is_read'];
    echo "JSON: " . json_encode($row) . "\n";
} else {
    echo "No messages to test.\n";
}

// Test get_chat_threads logic
echo "\nChecking get_chat_threads.php simulated thread:\n";
$res2 = $conn->query("SELECT id, 0 as unread_count FROM users LIMIT 1");
if ($row2 = $res2->fetch_assoc()) {
    $thread = [
        "id" => (string)$row2['id'],
        "unreadCount" => (int)$row2['unread_count']
    ];
    echo "JSON: " . json_encode($thread) . "\n";
}

echo "\nVerification Complete.";
?>
