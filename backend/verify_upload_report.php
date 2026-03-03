<?php
// Simulate a multipart POST request for file upload
$patient_id = 17;
$file_name = "test_report.pdf";
$file_content = "This is a test report content.";

// Use curl to simulate the upload
$ch = curl_init('http://localhost/genecare/upload_report.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);

// Create a temporary file
$tmp_file = tempnam(sys_get_temp_dir(), 'report');
file_put_contents($tmp_file, $file_content);

$cfile = new CURLFile($tmp_file, 'application/pdf', $file_name);
$data = [
    'patient_id' => $patient_id,
    'file' => $cfile
];

curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
$response = curl_exec($ch);
curl_close($ch);
unlink($tmp_file);

echo "Response from upload_report.php:\n";
echo $response . "\n\n";

include 'db_connect.php';
$check = $conn->query("SELECT * FROM patient_reports WHERE patient_id = $patient_id ORDER BY id DESC LIMIT 1");
if ($row = $check->fetch_assoc()) {
    echo "Verification Success! Data found in database:\n";
    print_r($row);
} else {
    echo "Verification Failed! No data found in database.\n";
}
?>
