<?php
ob_start();
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$full_name = $_POST['full_name'] ?? $data['full_name'] ?? null;
$email = $_POST['email'] ?? $data['email'] ?? null;
$password = $_POST['password'] ?? $data['password'] ?? null;
$user_type = $_POST['user_type'] ?? $data['user_type'] ?? 'Patient';


if (empty($full_name) || empty($email) || empty($password)) {
    ob_clean();
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit();
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    ob_clean();
    echo json_encode(["status" => "error", "message" => "Invalid email format"]);
    exit();
}

$password_hash = password_hash($password, PASSWORD_DEFAULT);

$stmt = $conn->prepare("INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)");

if (!$stmt) {
    ob_clean();
    echo json_encode(["status" => "error", "message" => "Prepare failed: " . $conn->error]);
    exit();
}

$stmt->bind_param("ssss", $full_name, $email, $password_hash, $user_type);

if ($stmt->execute()) {
    $user_id = $conn->insert_id;
    
    // Initialize the respective profile table to ensure data consistency
    if ($user_type === 'Patient') {
        $profile_stmt = $conn->prepare("INSERT INTO patient_profiles (user_id) VALUES (?)");
        $profile_stmt->bind_param("i", $user_id);
        $profile_stmt->execute();
        $profile_stmt->close();
    } else if ($user_type === 'Counselor') {
        $profile_stmt = $conn->prepare("INSERT INTO counselor_profiles (user_id) VALUES (?)");
        $profile_stmt->bind_param("i", $user_id);
        $profile_stmt->execute();
        $profile_stmt->close();
    }

    ob_clean();
    echo json_encode([
        "status" => "success",
        "message" => "User registered successfully",
        "user_id" => $user_id,
        "user" => [
            "id" => $user_id,
            "full_name" => $full_name,
            "user_type" => $user_type
        ]
    ]);
} else {
    ob_clean();
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$conn->close();
?>
