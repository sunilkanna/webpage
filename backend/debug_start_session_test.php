<?php
include 'db_connect.php';

// Mock values
$appointment_id = 65;
$user_id = 4; // Use a known user ID (admin or counselor)

try {
    // Fetch appointment details
    $stmt = $conn->prepare("SELECT a.* FROM appointments a WHERE a.id = ?");
    $stmt->bind_param("i", $appointment_id);
    $stmt->execute();
    $appointment = $stmt->get_result()->fetch_assoc();
    
    if (!$appointment) {
        die("Appointment 65 not found in DB");
    }
    
    echo "Found appointment: " . json_encode($appointment) . "\n\n";
    
    // Test Jitsi logic
    include_once 'jaas_config.php';
    include_once 'jwt_helper.php';
    
    echo "JAAS_APP_ID check: " . (defined('JAAS_APP_ID') ? JAAS_APP_ID : "NOT DEFINED") . "\n";
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>
