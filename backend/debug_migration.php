<?php
// Simulate a start_session.php request
$_POST = []; // Clear POST
$data = json_encode([
    "appointment_id" => 1, // Assuming ID 1 exists, we'll try to find a real one
    "user_id" => 1
]);

// We need to find a valid appointment ID first
include 'db_connect.php';
$res = $conn->query("SELECT id, patient_id FROM appointments ORDER BY id DESC LIMIT 1");
$row = $res->fetch_assoc();
if ($row) {
    $aid = $row['id'];
    $uid = $row['patient_id'];
    
    // Create actual request data
    $request_body = json_encode(["appointment_id" => $aid, "user_id" => $uid]);
    
    // Now we can't easily "include" start_session because it uses php://input
    // So we'll just check the DB logic manually in this script
    echo "Checking Appointment ID: $aid\n";
    $stmt = $conn->prepare("SELECT meeting_link, status FROM appointments WHERE id = ?");
    $stmt->bind_param("i", $aid);
    $stmt->execute();
    $result = $stmt->get_result();
    $appointment = $result->fetch_assoc();
    echo "Current Status: " . $appointment['status'] . "\n";
    echo "Current Link in DB: [" . $appointment['meeting_link'] . "]\n";
    
    $pos = strpos($appointment['meeting_link'], 'meet.jit.si');
    echo "strpos('meet.jit.si') result: " . var_export($pos, true) . "\n";
    
    // Run the migration logic here to see if it works
    if (empty($appointment['meeting_link']) || $pos !== false) {
        echo "Migration logic result: YES (Entering migration block)\n";
        if (!empty($appointment['meeting_link'])) {
            $new_link = str_replace('meet.jit.si', 'meet.ffmuc.net', $appointment['meeting_link']);
        } else {
            $new_link = "https://meet.ffmuc.net/genecare-test-" . bin2hex(random_bytes(4));
        }
        echo "New link candidate: $new_link\n";
    } else {
        echo "Migration logic result: NO (Condition failed)\n";
    }
} else {
    echo "No appointments found.\n";
}
$conn->close();
?>
