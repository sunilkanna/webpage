<?php
include 'db_connect.php';

$logs = [
    ['System recovery successful', 'SUCCESS', 'SystemCore'],
    ['New admin login detected from 192.168.1.1', 'INFO', 'AuthService'],
    ['Database backup completed successfully', 'SUCCESS', 'BackupManager'],
    ['Failed login attempt for user admin@genecare.com', 'WARNING', 'AuthService'],
    ['Server memory usage at 85%', 'WARNING', 'SystemMonitor'],
    ['New counselor application submitted: Dr. Smith', 'SUCCESS', 'CounselorModule'],
    ['API connection established with main server', 'INFO', 'NetworkLayer'],
    ['Critical error: Failed to connect to payment gateway', 'ERROR', 'PaymentService'],
];

foreach ($logs as $log) {
    $stmt = $conn->prepare("INSERT INTO system_logs (message, level, source) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $log[0], $log[1], $log[2]);
    $stmt->execute();
    $stmt->close();
}

echo "Database seeded with ".count($logs)." logs.";
$conn->close();
?>
