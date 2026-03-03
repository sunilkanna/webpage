<?php
include 'db_connect.php';

echo "<h2>Cleaning Duplicate Qualifications</h2>";

// This script finds users with multiple 'Pending' qualifications and keeps only the latest one
$sql = "DELETE cq1 FROM counselor_qualifications cq1
        INNER JOIN counselor_qualifications cq2 
        WHERE cq1.id < cq2.id 
        AND cq1.user_id = cq2.user_id 
        AND cq1.status = 'Pending' 
        AND cq2.status = 'Pending'";

if ($conn->query($sql)) {
    echo "<p style='color: green;'>Successfully removed redundant pending qualifications.</p>";
    echo "<p>Rows affected: " . $conn->affected_rows . "</p>";
} else {
    echo "<p style='color: red;'>Error cleaning duplicates: " . $conn->error . "</p>";
}

$conn->close();
?>
