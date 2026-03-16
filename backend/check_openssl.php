<?php
echo "OpenSSL enabled: " . (extension_loaded('openssl') ? "YES" : "NO") . "\n";
if (extension_loaded('openssl')) {
    echo "OpenSSL version: " . OPENSSL_VERSION_TEXT . "\n";
}
?>
