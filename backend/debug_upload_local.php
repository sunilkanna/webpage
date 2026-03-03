<?php
// Debug Upload Locally
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Mock POST
$_POST['patient_id'] = 1002;

// Mock FILES
$_FILES['file'] = [
    'name' => 'test_local.txt',
    'type' => 'text/plain',
    'tmp_name' => 'test_local_tmp.txt',
    'error' => 0,
    'size' => 123
];

// Create dummy tmp file
file_put_contents('test_local_tmp.txt', 'Test Content');

echo "Simulating upload...\n";

// Capture output
ob_start();
include 'upload_report.php';
$output = ob_get_clean();

echo "Output:\n" . $output . "\n";

// Cleanup
@unlink('test_local_tmp.txt');
?>
