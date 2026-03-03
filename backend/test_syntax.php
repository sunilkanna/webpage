<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
echo "Start include...\n";
try {
    include 'upload_report.php';
} catch (Throwable $e) {
    echo "Exception: " . $e->getMessage();
}
echo "End include.\n";
?>
