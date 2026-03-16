<?php
include 'db_connect.php';
error_reporting(E_ALL);
ini_set('display_errors', 1);
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

echo "<h1>Report Access Diagnosis</h1>";

// 1. Check Directory
$uploads_dir = 'uploads/';
echo "<h2>1. Directory Check: $uploads_dir</h2>";
if (file_exists($uploads_dir)) {
    echo "Exists: Yes<br>";
    echo "Permissions: " . substr(sprintf('%o', fileperms($uploads_dir)), -4) . "<br>";
    echo "Is Readable: " . (is_readable($uploads_dir) ? "Yes" : "No") . "<br>";
    echo "Is Writable: " . (is_writable($uploads_dir) ? "Yes" : "No") . "<br>";
} else {
    echo "Exists: No<br>";
}

// 2. Check .htaccess
$htaccess = $uploads_dir . '.htaccess';
echo "<h2>2. .htaccess Check</h2>";
if (file_exists($htaccess)) {
    echo "Exists: Yes<br>";
    echo "Content:<pre>" . htmlspecialchars(file_get_contents($htaccess)) . "</pre>";
} else {
    echo "Exists: No<br>";
}

// 3. Check Database URLs
echo "<h2>3. Sample URLs from Database</h2>";

echo "<h3>Appointments Table (medical_report_url)</h3>";
$res1 = $conn->query("SELECT id, medical_report_url FROM appointments WHERE medical_report_url IS NOT NULL LIMIT 5");
while ($row = $res1->fetch_assoc()) {
    echo "ID " . $row['id'] . ": " . htmlspecialchars($row['medical_report_url']) . "<br>";
}

echo "<h3>Patient Reports Table (file_url)</h3>";
$res2 = $conn->query("SELECT id, file_url FROM patient_reports LIMIT 5");
if ($res2) {
    while ($row = $res2->fetch_assoc()) {
        echo "ID " . $row['id'] . ": " . htmlspecialchars($row['file_url']) . "<br>";
    }
} else {
    echo "patient_reports table might not exist or empty.<br>";
}

// 4. Server Info
echo "<h2>4. Server Info</h2>";
echo "HTTP_HOST: " . $_SERVER['HTTP_HOST'] . "<br>";
echo "SERVER_ADDR: " . $_SERVER['SERVER_ADDR'] . "<br>";
echo "PHP Version: " . phpversion() . "<br>";

$conn->close();
?>
