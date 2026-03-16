<?php
include 'db_connect.php';
header("Access-Control-Allow-Origin: *");

// Simple proxy to serve files from the uploads directory
// Usage: view_report.php?file=filename.pdf

$file = $_GET['file'] ?? '';

// Basic security check: prevent directory traversal
$file = basename($file);
$filepath = 'uploads/' . $file;

if (empty($file) || !file_exists($filepath)) {
    header("HTTP/1.0 404 Not Found");
    echo "File not found.";
    exit;
}

// Determine MIME type
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$mime = finfo_file($finfo, $filepath);
finfo_close($finfo);

$is_download = isset($_GET['download']) && $_GET['download'] == '1';

header("Content-Type: $mime");
header("Content-Length: " . filesize($filepath));
if ($is_download) {
    header("Content-Disposition: attachment; filename=\"$file\"");
} else {
    header("Content-Disposition: inline; filename=\"$file\"");
}

readfile($filepath);
exit;
?>
