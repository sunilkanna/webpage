<?php
include 'db_connect.php';

// 1. Setup - Use a test patient and counselor
$patient_id = 17; // Existing test patient
$counselor_id = 1; // Existing test counselor

echo "Starting Notifications Verification...\n\n";

// 2. Trigger an Appointment Booking
echo "Step 1: Simulating an appointment booking...\n";
$ch = curl_init('http://localhost/genecare/book_appointment.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
    'patient_id' => $patient_id,
    'counselor_id' => $counselor_id,
    'date' => date('Y-m-d', strtotime('+1 day')),
    'time' => '11:00 AM'
]));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$resp = curl_exec($ch);
curl_close($ch);
echo "Booking Response: $resp\n";

// 3. Verify Counselor Notification
echo "\nStep 2: Checking counselor notifications...\n";
$ch = curl_init("http://localhost/genecare/get_notifications.php?user_id=$counselor_id");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$resp = curl_exec($ch);
curl_close($ch);
$notifs = json_decode($resp, true);

$found = false;
foreach ($notifs['notifications'] as $n) {
    if (strpos($n['title'], 'Appointment') !== false) {
        $found = true;
        echo "Found Notification: " . $n['message'] . " (ID: " . $n['id'] . ")\n";
        $notif_id = $n['id'];
        break;
    }
}

if ($found) {
    echo "SUCCESS: Counselor received booking notification.\n";
    
    // 4. Test Mark as Read
    echo "\nStep 3: Testing 'Mark as Read'...\n";
    $ch = curl_init('http://localhost/genecare/mark_notification_read.php');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['notification_id' => $notif_id]));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    $resp = curl_exec($ch);
    curl_close($ch);
    echo "Mark Read Response: $resp\n";
    
    // Verify in DB
    $res = $conn->query("SELECT is_read FROM notifications WHERE id = $notif_id");
    $row = $res->fetch_assoc();
    if ($row['is_read'] == 1) {
        echo "SUCCESS: Notification marked as read in database.\n";
    } else {
        echo "FAILURE: Notification still unread in database.\n";
    }
} else {
    echo "FAILURE: Booking notification not found for counselor.\n";
}

$conn->close();
?>
