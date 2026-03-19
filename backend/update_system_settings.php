<?php
include 'db_connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "No data provided"]);
    exit();
}

// Check if it's a bulk update (object of key-value pairs)
// If it has 'setting_key', it's a single update
if (isset($data['setting_key']) && isset($data['setting_value'])) {
    $stmt = $conn->prepare("UPDATE system_settings SET setting_value = ? WHERE setting_key = ?");
    $stmt->bind_param("ss", $data['setting_value'], $data['setting_key']);
    $stmt->execute();
    $stmt->close();
} else {
    // Bulk update
    $stmt = $conn->prepare("UPDATE system_settings SET setting_value = ? WHERE setting_key = ?");
    foreach ($data as $key => $value) {
        $stmt->bind_param("ss", $value, $key);
        $stmt->execute();
    }
    $stmt->close();
}

echo json_encode(["status" => "success", "message" => "Settings updated successfully"]);

$stmt->close();
$conn->close();
?>
