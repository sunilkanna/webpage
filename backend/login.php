<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$email = $_POST['email'] ?? $data['email'] ?? null;
$password = $_POST['password'] ?? $data['password'] ?? null;

$stmt = $conn->prepare("SELECT u.id, u.full_name, u.password_hash, u.user_type, cq.status as verification_status FROM users u LEFT JOIN counselor_qualifications cq ON u.id = cq.user_id WHERE u.email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row['password_hash'])) {
        echo json_encode([
            "status" => "success", 
            "message" => "Login successful",
            "user" => [
                "id" => $row['id'],
                "full_name" => $row['full_name'],
                "user_type" => $row['user_type'],
                "verification_status" => $row['verification_status'] // Return status to app
            ]
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$stmt->close();
$conn->close();
?>
