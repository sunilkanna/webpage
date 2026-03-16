<?php
$conn = new mysqli("127.0.0.1", "root", "", "genecare_db");
if ($conn->connect_error) die("DB Connection Error: " . $conn->connect_error);

$appointment_id = 65;
$user_id = 1013; // Counselor

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
    $result = $stmt->get_result();
    $appointment = $result->fetch_assoc();
    
    if ($appointment) {
        echo "SUCCESS: Query returned data.\n";
        echo "Patient: " . $appointment['patient_name'] . "\n";
        echo "Counselor: " . $appointment['counselor_name'] . "\n";
        echo "Fee: " . $appointment['consultation_fee'] . "\n";
    } else {
        echo "FAILURE: Query returned no data (possibly missing users).\n";
    }
} catch (Exception $e) {
    echo "Query Error: " . $e->getMessage();
}
$conn->close();
?>
