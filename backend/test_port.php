<?php
$host = '127.0.0.1';
$port = 3306;
$timeout = 5;

$fp = @fsockopen($host, $port, $errno, $errstr, $timeout);
if (!$fp) {
    echo "PORT_CLOSED: $errstr ($errno)";
} else {
    echo "PORT_OPEN";
    fclose($fp);
}
?>
