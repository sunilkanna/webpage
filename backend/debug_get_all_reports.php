<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Mock server environment if needed, but just inclusion should work if no session/auth check is strict
// get_all_reports.php only includes db_connect.php

ob_start();
include 'get_all_reports.php';
$output = ob_get_clean();

$data = json_decode($output, true);

if (json_last_error() !== JSON_ERROR_NONE) {
    echo "JSON Error: " . json_last_error_msg() . "\n";
    echo "Raw Output: \n" . $output;
} else {
    echo "JSON Success\n";
    print_r($data);
}
?>
