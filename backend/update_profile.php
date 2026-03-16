<?php
include 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true);

$user_id = $_POST['user_id'] ?? $data['user_id'] ?? null;
$full_name = trim($_POST['full_name'] ?? $data['full_name'] ?? '');
$dob = trim($_POST['date_of_birth'] ?? $data['date_of_birth'] ?? '');
$gender = trim($_POST['gender'] ?? $data['gender'] ?? '');
$phone = preg_replace('/[^0-9]/', '', $_POST['phone'] ?? $data['phone'] ?? ''); // Remove non-numeric
$address = trim($_POST['address'] ?? $data['address'] ?? '');
$height = trim($_POST['height'] ?? $data['height'] ?? '');
$weight = trim($_POST['weight'] ?? $data['weight'] ?? '');
$blood_type = trim($_POST['blood_type'] ?? $data['blood_type'] ?? '');

if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "User ID is required"]);
    exit();
}

// Basic Backend Validation
if (!empty($dob) && strtotime($dob) > time()) {
    echo json_encode(["status" => "error", "message" => "Date of birth cannot be in the future"]);
    exit();
}

if (!empty($phone) && (strlen($phone) < 10 || strlen($phone) > 15)) {
    echo json_encode(["status" => "error", "message" => "Invalid phone number format"]);
    exit();
}

// Start transaction to update both tables
$conn->begin_transaction();

try {
    // 1. Update Full Name in 'users' table if provided
    if ($full_name) {
        $stmt1 = $conn->prepare("UPDATE users SET full_name = ? WHERE id = ?");
        $stmt1->bind_param("si", $full_name, $user_id);
        $stmt1->execute();
        $stmt1->close();
    }

    // 2. Insert/Update fields in 'patient_profiles' table
    $stmt2 = $conn->prepare("INSERT INTO patient_profiles (user_id, date_of_birth, gender, phone, address, height, weight, blood_type) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE date_of_birth=?, gender=?, phone=?, address=?, height=?, weight=?, blood_type=?");
    $stmt2->bind_param("issssssssssssss", 
        $user_id, $dob, $gender, $phone, $address, $height, $weight, $blood_type,
        $dob, $gender, $phone, $address, $height, $weight, $blood_type
    );
    $stmt2->execute();
    $stmt2->close();

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Profile updated successfully"]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => "Error updating profile: " . $e->getMessage()]);
}

$conn->close();
?>
