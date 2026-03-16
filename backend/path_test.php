<?php
echo "Current File: " . __FILE__ . "\n";
include 'db_connect.php';
echo "DB Connect File: " . (new ReflectionFunction('mysqli_report'))->getFileName() . " ... wait no\n";
// Actually just show db_connect.php content or path
echo "Included files:\n";
print_r(get_included_files());
?>
