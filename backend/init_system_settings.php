<?php
include 'db_connect.php';

echo "<h2>System Settings Table Initialization</h2>";

$table = "system_settings";
$result = $conn->query("SHOW TABLES LIKE '$table'");

if ($result->num_rows == 0) {
    echo "<p>Creating table <b>$table</b>...</p>";
    
    $create = "CREATE TABLE system_settings (
        setting_key VARCHAR(50) PRIMARY KEY,
        setting_value VARCHAR(255) NOT NULL,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )";
    
    if ($conn->query($create)) {
        echo "<p style='color: green;'>Table created successfully.</p>";
        
        // Initialize default values
        $defaults = [
            ['maintenance_mode', '0'],
            ['email_alerts', '1'],
            ['push_notifications', '1'],
            ['dark_mode', '0'],
            ['two_factor_auth', '1'],
            ['gst_percentage', '5']
        ];
        
        $stmt = $conn->prepare("INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?)");
        foreach ($defaults as $default) {
            $stmt->bind_param("ss", $default[0], $default[1]);
            $stmt->execute();
        }
        $stmt->close();
        echo "<p style='color: green;'>Default settings initialized.</p>";
    } else {
        echo "<p style='color: red;'>Failed to create table: " . $conn->error . "</p>";
    }
} else {
    echo "<p style='color: blue;'>Table <b>$table</b> already exists.</p>";
}

$conn->close();
?>
