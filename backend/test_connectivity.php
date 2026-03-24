<?php
$servername = "127.0.0.1";
$username = "root";
$password = "";
$dbname = "genecare_db";

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        echo "FAIL: " . $conn->connect_error;
    } else {
        echo "SUCCESS";
    }
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage();
}
?>
