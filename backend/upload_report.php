<?php
include 'db_connect.php';

$patient_id = $_POST['patient_id'];
$target_dir = "uploads/";
if (!file_exists($target_dir)) {
    mkdir($target_dir, 0777, true);
}

$file_name = basename($_FILES["file"]["name"]);
$unique_prefix = time() . "_" . bin2hex(random_bytes(4));
$target_file = $target_dir . $unique_prefix . "_" . $file_name;
$uploadOk = 1;

// Check if file is provided
if(isset($_FILES["file"])) {
    if (move_uploaded_file($_FILES["file"]["tmp_name"], $target_file)) {
        // Dynamic server IP detection or use localhost for dev
            $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
            $host = $_SERVER['HTTP_HOST']; 
            $saved_name = $unique_prefix . "_" . $file_name;
            $file_url = $protocol . "://" . $host . "/genecare/view_report.php?file=" . urlencode($saved_name);

        $stmt = $conn->prepare("INSERT INTO patient_reports (patient_id, file_name, file_url) VALUES (?, ?, ?)");
        $stmt->bind_param("iss", $patient_id, $file_name, $file_url);
        
        if ($stmt->execute()) {
            // Create notification for patient
            $title = "Report Uploaded";
            $message = "Your report '$file_name' has been uploaded successfully.";
            $notify = $conn->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'Report')");
            $notify->bind_param("iss", $patient_id, $title, $message);
            $notify->execute();
            $notify->close();

            echo json_encode(["status" => "success", "message" => "File uploaded", "file_url" => $file_url]);
        } else {
             echo json_encode(["status" => "error", "message" => "Database error: " . $stmt->error]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Error uploading file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "No file received"]);
}

$conn->close();
?>
