<?php
$host = '127.0.0.1';
$user = 'root';
$pass = '';

try {
    $conn = new mysqli($host, $user, $pass);
    if ($conn->connect_error) {
        echo "CONNECT_FAILED: " . $conn->connect_error;
    } else {
        echo "CONNECT_SUCCESS";
        $conn->close();
    }
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage();
}
?>
