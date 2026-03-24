<?php
// get_system_settings.php - Robust Version
include 'db_connect.php';
header('Content-Type: application/json');

try {
    $result = $conn->query("SELECT setting_key, setting_value FROM system_settings");
    $settings = [];
    while ($row = $result->fetch_assoc()) {
        $settings[$row['setting_key']] = $row['setting_value'];
    }
    echo json_encode(["status" => "success", "settings" => $settings]);
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}

$conn->close();
?>
