<?php
/**
 * DEBUG TOOL: Reset Session for Testing
 * Usage: open debug_reset_session.php?id=44 in your browser
 * 
 * This script resets an appointment to 'Confirmed' status, clears the meeting link,
 * and removes the start time so you can test the 'Join' flow from scratch.
 */
include 'db_connect.php';

$id = $_GET['id'] ?? null;

if (!$id) {
    die("Error: Please provide an appointment id (e.g., debug_reset_session.php?id=44)");
}

// 1. Reset the appointment record
$stmt = $conn->prepare("UPDATE appointments SET 
                        status = 'Confirmed', 
                        meeting_link = NULL, 
                        session_start_time = NULL 
                        WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo "<h1>Session Reset Successful</h1>";
    echo "<p>Appointment ID $id has been reset. You can now:</p>";
    echo "<ul>
            <li>Join as Counselor from the app</li>
            <li>Join as Patient from the app</li>
          </ul>";
    echo "<p>They should now both join the same room: <b>genecare-room-$id</b></p>";
} else {
    echo "Error resetting session: " . $conn->error;
}

$stmt->close();
$conn->close();
?>
