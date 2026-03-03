<?php
include 'db_connect.php';

// 1. Add columns if they don't exist
$alter_patient = "ALTER TABLE appointments ADD COLUMN IF NOT EXISTS patient_name VARCHAR(100) AFTER patient_id";
$alter_counselor = "ALTER TABLE appointments ADD COLUMN IF NOT EXISTS counselor_name VARCHAR(100) AFTER counselor_id";

if ($conn->query($alter_patient)) echo "Added patient_name column.\n";
if ($conn->query($alter_counselor)) echo "Added counselor_name column.\n";

// 2. Populate names for existing records
$update_sql = "
    UPDATE appointments a
    JOIN users up ON a.patient_id = up.id
    JOIN users uc ON a.counselor_id = uc.id
    SET a.patient_name = up.full_name,
        a.counselor_name = uc.full_name
";

if ($conn->query($update_sql)) {
    echo "Successfully populated names for existing appointments.\n";
} else {
    echo "Error populating names: " . $conn->error . "\n";
}

$conn->close();
?>
