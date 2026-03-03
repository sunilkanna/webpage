<?php
include 'db_connect.php';

$patientId = 1004;
$counselorId = 39;
$testMsg = "Final verification message at " . date('H:i:s');

// 1. Manually insert message (bypass send_message.php if needed, but we check logic)
$sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iis", $patientId, $counselorId, $testMsg);
$stmt->execute();
$newId = $conn->insert_id;

echo "Message Send Simulation: SUCCESS (ID: $newId)\n";

// 2. Counselor retrieves threads
echo "Counselor 39 retrieving threads...\n";
ob_start();
$_GET['counselor_id'] = $counselorId;
include 'get_chat_threads.php';
$threadsRes = json_decode(ob_get_clean(), true);

$found = false;
foreach($threadsRes['threads'] ?? [] as $t) {
    if($t['id'] == $patientId) {
        $found = true;
        echo "Patient 1004 found in threads. Last Message: " . $t['lastMessage'] . "\n";
        if($t['lastMessage'] === $testMsg) echo "CONTENT MATCH: YES\n";
        else echo "CONTENT MATCH: NO (Got: " . $t['lastMessage'] . ")\n";
    }
}

// 3. Counselor retrieves history
echo "\nCounselor 39 retrieving history with 1004...\n";
ob_start();
$_GET['user_id'] = $counselorId;
$_GET['other_user_id'] = $patientId;
include 'get_messages.php';
$historyRes = json_decode(ob_get_clean(), true);

$msgReceived = false;
foreach($historyRes['messages'] ?? [] as $m) {
    if($m['id'] == $newId) {
        $msgReceived = true;
        echo "Specific message found in history: YES\n";
    }
}

echo "\nResult: " . (($found && $msgReceived) ? "PASSED" : "FAILED") . "\n";
?>
