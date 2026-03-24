<?php
// db_connect.php - Clean Version
error_reporting(0); 
ini_set('display_errors', 0);
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "genecare_db";

define('WEBSITE_URL', 'http://172.20.10.2:5173');

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        die(json_encode(["status" => "error", "message" => "Connection failed: " . $conn->connect_error]));
    }
    date_default_timezone_set('Asia/Kolkata');
    $conn->query("SET time_zone = '+05:30'");
} catch (Exception $e) {
    die(json_encode(["status" => "error", "message" => $e->getMessage()]));
}
?>
