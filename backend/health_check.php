<?php
// health_check.php
header('Content-Type: application/json');
require_once __DIR__ . '/db_connect.php';

if (isset($conn) && $conn->ping()) {
    echo json_encode([
        "status" => "success",
        "message" => "Backend is working! Database connection established.",
        "server_ip" => $_SERVER['SERVER_ADDR']
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed"
    ]);
}
if (isset($conn)) {
    $conn->close();
}
?>
