<?php
include 'db_connect.php';

function test_post($url, $data) {
    global $conn;
    $fullUrl = "http://localhost/genecare/" . $url;
    $ch = curl_init($fullUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

function test_get($url, $params = []) {
    $queryString = http_build_query($params);
    $fullUrl = "http://localhost/genecare/" . $url . ($queryString ? "?" . $queryString : "");
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $fullUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

echo "--- Full Chat Flow Verification ---\n\n";

$patientId = 1004;
$counselorId = 39;
$randomUserId = 2; // No session with 1004

// 1. Patient sends message to Counselor
echo "Step 1: Patient (1004) sends message to Counselor (39)...\n";
$msgText = "Hello Counselor, testing the flow at " . date('H:i:s');
$res1 = test_post("send_message.php", [
    "sender_id" => $patientId,
    "receiver_id" => $counselorId,
    "message_text" => $msgText
]);
echo "Result: " . ($res1['status'] === 'success' ? "SUCCESS" : "FAILED (" . $res1['message'] . ")") . "\n\n";

// 2. Counselor lists threads
echo "Step 2: Counselor (39) fetches chat threads...\n";
$res2 = test_get("get_chat_threads.php", ["counselor_id" => $counselorId]);
$found = false;
foreach($res2['threads'] ?? [] as $t) {
    if($t['id'] == $patientId) {
        $found = true;
        echo "Found thread with Patient (1004). Last message: " . $t['lastMessage'] . "\n";
    }
}
echo "Result: " . ($found ? "SUCCESS" : "FAILED (Patient not in threads)") . "\n\n";

// 3. Counselor fetches messages
echo "Step 3: Counselor (39) fetches message history with Patient (1004)...\n";
$res3 = test_get("get_messages.php", ["user_id" => $counselorId, "other_user_id" => $patientId]);
$lastMsg = end($res3['messages'] ?? []);
echo "Result: " . ($lastMsg['message_text'] === $msgText ? "SUCCESS (Message received)" : "FAILED (Message mismatch)") . "\n\n";

// 4. Security: Patient tries to send to unauthorized user
echo "Step 4: Security Check - Patient (1004) sends to unauthorized User (2)...\n";
$res4 = test_post("send_message.php", [
    "sender_id" => $patientId,
    "receiver_id" => $randomUserId,
    "message_text" => "Unauthorized attempt"
]);
echo "Result: " . ($res4['status'] === 'error' ? "SUCCESS (Blocked: " . $res4['message'] . ")" : "FAILED (Allowed!)") . "\n\n";

echo "--- Verification Complete ---";
?>
