<?php
// Mock the data start_session.php expect
$appointment_id = 65;
$user_id = 1013;

echo "MOCKING INPUT: appointment_id=$appointment_id, user_id=$user_id\n\n";

include 'db_connect.php';

// Capture output to avoid header issues
ob_start();

try {
    // Fetch appointment details
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
    $stmt->close();

    if (!$appointment) {
        throw new Exception("Appointment not found");
    }

    $is_counselor = ($appointment['counselor_id'] == $user_id);
    $user_name = $is_counselor ? $appointment['counselor_name'] : $appointment['patient_name'];
    $room_name = "genecare-room-" . $appointment_id;

    include_once 'jaas_config.php';
    include_once 'jwt_helper.php';

    $jwt = null;
    if (defined('JAAS_APP_ID')) {
        $jwt = generateJitsiJwt(
            JAAS_APP_ID,
            JAAS_KEY_ID,
            JAAS_PRIVATE_KEY,
            $room_name,
            $user_name,
            $is_counselor,
            $user_id
        );
    }

    echo "RESULT_JSON: " . json_encode([
        "status" => "success",
        "jwt" => $jwt ? "GENERATED" : "NULL",
        "jaas_app_id" => defined('JAAS_APP_ID') ? JAAS_APP_ID : null,
        "patient_name" => $appointment['patient_name']
    ]);

} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage();
}

$output = ob_get_clean();
echo $output;
?>
