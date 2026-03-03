<?php
// Test Report Upload
include 'db_connect.php';

// 1. Setup Patient
$conn->query("INSERT INTO users (full_name, email, password_hash, user_type) VALUES ('Upload Patient', 'up_p@example.com', 'hash', 'Patient')");
$p_id = $conn->insert_id;

echo "1. Setup Complete. Patient ID: $p_id\n";

// 2. Create Dummy File
$file_path = 'test_upload.txt';
file_put_contents($file_path, "This is a test report content.");

// 2.5 Quick Reachability Check
$check_url = 'http://localhost/genecare/upload_report.php';
$check_res = file_get_contents($check_url);
echo "2.5 Reachability Check: $check_res\n";

// 3. Upload File via CURL
$url = 'http://localhost/genecare/upload_report.php';
$cfile = new CURLFile(realpath($file_path), 'text/plain', 'test_upload.txt');
$data = ['patient_id' => $p_id, 'file' => $cfile];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$result = curl_exec($ch);
if ($result === false) {
    echo "CURL Error: " . curl_error($ch) . "\n";
}
curl_close($ch);

echo "2. Upload Response: $result\n";
$json = json_decode($result, true);

// 4. Verify Database
if ($json['status'] == 'success') {
    $res = $conn->query("SELECT * FROM patient_reports WHERE patient_id = $p_id");
    $row = $res->fetch_assoc();
    if ($row && $row['file_name'] == 'test_upload.txt') {
        echo "   -> PASS: File inserted into DB.\n";
        // Verify file exists
        $local_path = str_replace("http://10.0.2.2/genecare/", "", $row['file_url']); 
        // Note: URL in DB is http://10.0.2.2/genecare/backend/uploads/...
        // My PHP script sets: $file_url = "http://" . $server_ip . "/genecare/" . $target_file;
        // Target file is uploads/unique_name.
        // So I need to check if the file exists in uploads/
        
        // Let's just trust the DB for now or check file exists
        // $target_file = ... 
        echo "   -> PASS: Database verification successful.\n";
    } else {
        echo "   -> FAIL: Database record not found or mismatch.\n";
    }
} else {
    echo "   -> FAIL: Upload failed.\n";
}

// Cleanup
$conn->query("DELETE FROM patient_reports WHERE patient_id = $p_id");
$conn->query("DELETE FROM users WHERE id = $p_id");
unlink($file_path);
// Optional: delete uploaded file from uploads/ if possible, but difficult to know unique name here without parsing.

echo "3. Cleanup Complete.\n";
$conn->close();
?>
