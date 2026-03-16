<?php
$conn = mysqli_init();
mysqli_options($conn, MYSQLI_OPT_CONNECT_TIMEOUT, 2);
echo "ATTEMPT_CONNECT_3307\n";
if (!@mysqli_real_connect($conn, '127.0.0.1', 'root', '', '', 3307)) {
    echo "FAILED_FAST: " . mysqli_connect_error() . "\n";
} else {
    echo "SUCCESS_??\n";
    mysqli_close($conn);
}
?>
