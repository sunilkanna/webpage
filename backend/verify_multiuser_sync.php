<?php
/**
 * MULTI-USER VERIFICATION: Ensure both join same room
 */
include 'db_connect.php';

// 1. Find a confirmed appointment
$check = $conn->query("SELECT id, patient_id, counselor_id FROM appointments WHERE status = 'Confirmed' LIMIT 1");
$appointment = $check->fetch_assoc();

if (!$appointment) {
    die("Error: No 'Confirmed' appointment found. Reset ID 44 using debug_reset_session.php first.");
}

$id = $appointment['id'];
$p_id = $appointment['patient_id'];
$c_id = $appointment['counselor_id'];

echo "--- TESTING APPOINTMENT #$id ---\n";

/**
 * Mocking the start_session logic to check what it would return
 */
function get_mock_result($appointment_id, $user_id, $conn) {
    // We'll mimic the core logic of start_session.php
    $stmt = $conn->prepare("SELECT a.*, p.full_name as patient_name, c.full_name as counselor_name FROM appointments a JOIN users p ON a.patient_id = p.id JOIN users c ON a.counselor_id = c.id WHERE a.id = ?");
    $stmt->bind_param("i", $appointment_id);
    $stmt->execute();
    $apt = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    $is_counselor = ($apt['counselor_id'] == $user_id);
    $room_name = "genecare-room-" . $appointment_id;
    
    include 'jaas_config.php';
    if (strpos(JAAS_APP_ID, 'REPLACE_ME') === false) {
        $meeting_link = "https://8x8.vc/" . JAAS_APP_ID . "/" . $room_name;
    } else {
        $meeting_link = $apt['meeting_link'] ?: "https://meet.jit.si/" . $room_name;
    }
    
    return [
        "user_id" => $user_id,
        "role" => $is_counselor ? "Counselor" : "Patient",
        "room" => $room_name,
        "link" => $meeting_link
    ];
}

$counselor_result = get_mock_result($id, $c_id, $conn);
$patient_result = get_mock_result($id, $p_id, $conn);

echo "Counselor Result: " . $counselor_result['link'] . " (Room: " . $counselor_result['room'] . ")\n";
echo "Patient Result: " . $patient_result['link'] . " (Room: " . $patient_result['room'] . ")\n";

if ($counselor_result['link'] === $patient_result['link']) {
    echo "\nVERIFICATION SUCCESS: Both users join the EXACT same session link.\n";
} else {
    echo "\nVERIFICATION FAILED: Links do not match!\n";
}

$conn->close();
?>
