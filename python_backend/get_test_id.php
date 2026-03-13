<?php
include 'c:/xampp/htdocs/genecare/db_connect.php';
$res = $conn->query("SELECT id, user_type FROM users WHERE user_type='patient' LIMIT 1");
if ($res && $row = $res->fetch_assoc()) {
    echo $row['id'];
} else {
    echo "No patient found";
}
?>
