<?php
include 'db_connect.php';

echo "--- DEBUG: RECENT BOOKINGS (Last 24 Hours) ---\n";

$sql = "SELECT a.id, a.patient_id, a.counselor_id, a.appointment_date, a.time_slot, a.status, a.created_at, a.meeting_link,
               p.full_name as patient_name, c.full_name as counselor_name
        FROM appointments a
        LEFT JOIN users p ON a.patient_id = p.id
        LEFT JOIN users c ON a.counselor_id = c.id
        ORDER BY a.id DESC LIMIT 5";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        echo "ID: " . $row['id'] . "\n";
        echo "   Patient: " . ($row['patient_name'] ?? 'Unknown') . " (ID: " . $row['patient_id'] . ")\n";
        echo "   Counselor: " . ($row['counselor_name'] ?? 'Unknown') . " (ID: " . $row['counselor_id'] . ")\n";
        echo "   Date: " . $row['appointment_date'] . " Time: " . $row['time_slot'] . "\n";
        echo "   Status: " . $row['status'] . "\n";
        echo "   Link: " . $row['meeting_link'] . "\n";
        echo "   Created: " . $row['created_at'] . "\n";
        echo "------------------------------------------------\n";
    }
} else {
    echo "No appointments found in the last 24 hours.\n";
}

// Also check total count to be sure
$count = $conn->query("SELECT COUNT(*) as c FROM appointments")->fetch_assoc()['c'];
echo "\nTotal Appointments in DB: $count\n";

$conn->close();
?>
