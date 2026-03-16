<?php
echo "mysqli extension: " . (extension_loaded('mysqli') ? "LOADED" : "MISSING") . "\n";
$conn = mysqli_init();
if ($conn) {
    echo "mysqli_init: SUCCESS\n";
} else {
    echo "mysqli_init: FAILED\n";
}
?>
