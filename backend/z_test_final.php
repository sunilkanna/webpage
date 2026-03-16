<?php
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
$conn = new mysqli("127.0.0.1", "root", "", "genecare_db");

$appointment_id = 65;

try {
    $stmt = $conn->prepare("SELECT a.*, 
        p.full_name as patient_name, 
        c.full_name as counselor_name,
        cp.consultation_fee
        FROM appointments a
        JOIN users p ON a.patient_id = p.id
        JOIN users c ON a.counselor_id = c.id
        LEFT JOIN counselor_profiles cp ON c.id = cp.user_id
        WHERE a.id = ?");
    $stmt->bind_param("i", $appointment_id);
    $stmt->execute();
    $appointment = $stmt->get_result()->fetch_assoc();
    
    if ($appointment) {
        echo "DATABASE_OK\n";
        echo "Patient: " . $appointment['patient_name'] . "\n";
        
        include_once 'jaas_config.php';
        include_once 'jwt_helper.php';
        
        if (defined('JAAS_APP_ID')) {
            echo "JAAS_CONFIGURED\n";
            echo "App ID: " . JAAS_APP_ID . "\n";
        } else {
            echo "JAAS_NOT_DEFINED\n";
        }
        
    } else {
        echo "DATABASE_EMPTY\n";
    }
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
$conn->close();
?>
