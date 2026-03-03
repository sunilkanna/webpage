<?php
include 'db_connect.php';

function test_endpoint($url, $params = []) {
    $queryString = http_build_query($params);
    $fullUrl = "http://localhost/genecare/" . $url . ($queryString ? "?" . $queryString : "");
    echo "Testing $fullUrl\n";
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $fullUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

echo "--- Testing Session-Restricted Messaging ---\n\n";

// 1. Try to get messages for a non-existent session
$res1 = test_endpoint("get_messages.php", ["user_id" => 1, "other_user_id" => 999]);
echo "Get Messages (No Session): " . ($res1['status'] === 'error' ? "PASSED (Rejected)" : "FAILED (Allowed)") . " - " . ($res1['message'] ?? 'No message') . "\n";

// 2. Try to get chat threads for a counselor
$res2 = test_endpoint("get_chat_threads.php", ["counselor_id" => 2]);
echo "Get Chat Threads: " . ($res2['status'] === 'success' ? "PASSED" : "FAILED") . " - Found " . count($res2['threads'] ?? []) . " threads.\n";

// 3. Try to send a message without a session
$post_data = ["sender_id" => 1, "receiver_id" => 999, "message_text" => "Hack attempt"];
$ch = curl_init("http://localhost/genecare/send_message.php");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($post_data));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$res3 = json_decode(curl_exec($ch), true);
curl_close($ch);
echo "Send Message (No Session): " . ($res3['status'] === 'error' ? "PASSED (Rejected)" : "FAILED (Allowed)") . " - " . ($res3['message'] ?? 'No message') . "\n";

echo "\n--- Verification Complete ---\n";
?>
