<?php
echo "Current directory: " . __DIR__ . "\n";
if (file_exists('db_connect.php')) {
    echo "db_connect.php exists\n";
    echo "Content:\n";
    echo file_get_contents('db_connect.php');
} else {
    echo "db_connect.php DOES NOT exist\n";
}
?>
