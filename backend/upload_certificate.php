<?php
include 'db_connect.php';

// Security: No session check here because this is part of the registration flow
// but we should ensure it's a POST request with a file

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit();
}

$target_dir = "uploads/certificates/";
if (!file_exists($target_dir)) {
    mkdir($target_dir, 0777, true);
}

if (!isset($_FILES["certificate"])) {
    echo json_encode(["status" => "error", "message" => "No certificate file provided"]);
    exit();
}

$file = $_FILES["certificate"];
$file_name = basename($file["name"]);
$file_ext = strtolower(pathinfo($file_name, PATHINFO_EXTENSION));

// Allowed formats
$allowed_extensions = ['pdf', 'jpg', 'jpeg', 'png'];
if (!in_array($file_ext, $allowed_extensions)) {
    echo json_encode(["status" => "error", "message" => "Invalid file format. Only PDF, JPG, and PNG are allowed."]);
    exit();
}

// Max 5MB
if ($file["size"] > 5 * 1024 * 1024) {
    echo json_encode(["status" => "error", "message" => "File size exceeds 5MB limit."]);
    exit();
}

$unique_prefix = time() . "_" . bin2hex(random_bytes(4));
$saved_name = $unique_prefix . "." . $file_ext;
$target_file = $target_dir . $saved_name;

if (move_uploaded_file($file["tmp_name"], $target_file)) {
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
    $host = $_SERVER['HTTP_HOST'];
    $file_url = $protocol . "://" . $host . "/genecare/view_report.php?file=" . urlencode("certificates/" . $saved_name);

    echo json_encode([
        "status" => "success",
        "message" => "Certificate uploaded successfully",
        "file_url" => $file_url,
        "file_name" => $file_name
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to save file on server."]);
}

$conn->close();
?>
