<?php
/**
 * VERIFICATION SCRIPT: Test start_session.php logic
 * This script manually sets up a mock environment to test the backend response.
 */

// 1. Find a valid confirmed appointment ID first
include 'db_connect.php';
$check = $conn->query("SELECT id, patient_id, counselor_id FROM appointments WHERE status = 'Confirmed' LIMIT 1");
$appointment = $check->fetch_assoc();

if (!$appointment) {
    die("Error: No 'Confirmed' appointment found in database to test with. Please create one or use debug_reset_session.php?id=44 first.");
}

$id = $appointment['id'];
$user_id = $appointment['counselor_id']; // Testing as counselor (moderator)

echo "--- VERIFYING JOIN SESSION (ID: $id, User: $user_id) ---\n";

// 2. Simulate the POST data that start_session.php expects via php://input
// Since we can't easily mock php://input in a CLI script directly, 
// we'll use a wrapper approach or manually check the logic.
// For this test, we'll verify the helper files directly.

include 'jaas_config.php';
include 'jwt_helper.php';

$room_name = "genecare-room-" . $id;
$user_name = "Test User";
$is_moderator = true;

echo "Configuring Room: $room_name\n";

if (strpos(JAAS_APP_ID, 'REPLACE_ME') !== false) {
    echo "RESULT: JaaS NOT configured (using fallback link)\n";
} else {
    echo "RESULT: JaaS Configured. Generating JWT...\n";
    $jwt = generateJitsiJwt(
        JAAS_APP_ID,
        JAAS_KEY_ID,
        JAAS_PRIVATE_KEY,
        $room_name,
        $user_name,
        $is_moderator,
        $user_id
    );
    
    if ($jwt) {
        echo "JWT Generated Successfully: " . substr($jwt, 0, 20) . "...\n";
        
        // Decode JWT to verify claims
        $parts = explode('.', $jwt);
        $payload = json_decode(base64_decode($parts[1]), true);
        
        echo "Claim Check (Room): " . $payload['room'] . "\n";
        echo "Claim Check (Moderator): " . ($payload['context']['user']['moderator'] ? 'TRUE' : 'FALSE') . "\n";
        echo "Claim Check (User ID): " . $payload['context']['user']['id'] . "\n";
        
        if ($payload['room'] === "*") {
            echo "VERIFICATION: SUCCESS (Wildcard Room Active)\n";
        } else {
            echo "VERIFICATION: WARNING (Room claim should be '*' for max compatibility)\n";
        }
    } else {
        echo "VERIFICATION: FAILED (JWT Generation returned null)\n";
    }
}

$conn->close();
?>
