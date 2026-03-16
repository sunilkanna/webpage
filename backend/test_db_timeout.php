<?php
$conn = mysqli_init();
mysqli_options($conn, MYSQLI_OPT_CONNECT_TIMEOUT, 2);

echo "START_CONNECT\n";
if (!@mysqli_real_connect($conn, '127.0.0.1', 'root', '', 'genecare_db')) {
    echo "CONNECT_FAILED: " . mysqli_connect_error() . "\n";
} else {
    echo "CONNECT_SUCCESS\n";
    mysqli_close($conn);
}
?>
