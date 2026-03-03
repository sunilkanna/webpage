<?php
header('Content-Type: text/html; charset=utf-8');
include 'db_connect.php';

echo "<h2>GeneCare Database Migration</h2>";

$tables = [
    "patient_reports" => "CREATE TABLE IF NOT EXISTS patient_reports (
        id INT AUTO_INCREMENT PRIMARY KEY,
        patient_id INT NOT NULL,
        file_name VARCHAR(255),
        file_url VARCHAR(255) NOT NULL,
        uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
    )",
    "notifications" => "CREATE TABLE IF NOT EXISTS notifications (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        title VARCHAR(100),
        message TEXT,
        type VARCHAR(50) DEFAULT 'General',
        is_read BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )",
    "system_logs" => "CREATE TABLE IF NOT EXISTS system_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        message TEXT NOT NULL,
        level ENUM('INFO', 'WARNING', 'ERROR', 'SUCCESS') DEFAULT 'INFO',
        source VARCHAR(100),
        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )",
    "counselor_reports" => "CREATE TABLE IF NOT EXISTS counselor_reports (
        id INT AUTO_INCREMENT PRIMARY KEY,
        counselor_id INT NOT NULL,
        title VARCHAR(100),
        report_type VARCHAR(50),
        category VARCHAR(50),
        report_date DATE,
        file_url VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE
    )",
    "payments" => "CREATE TABLE IF NOT EXISTS payments (
        id INT AUTO_INCREMENT PRIMARY KEY,
        appointment_id INT NOT NULL,
        amount DECIMAL(10, 2) NOT NULL,
        payment_method VARCHAR(50),
        transaction_id VARCHAR(100),
        status ENUM('Pending', 'Completed', 'Failed') DEFAULT 'Pending',
        payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
    )"
];

foreach ($tables as $name => $sql) {
    echo "Checking table <b>$name</b>... ";
    if ($conn->query($sql) === TRUE) {
        echo "<span style='color: green;'>OK (Created or already exists)</span><br>";
    } else {
        echo "<span style='color: red;'>Error: " . $conn->error . "</span><br>";
    }
}

echo "<br><b>Database migration completed.</b>";
$conn->close();
?>
