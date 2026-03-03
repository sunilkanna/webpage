<?php
include 'db_connect.php';

// Total Users
$total_users = $conn->query("SELECT COUNT(*) as count FROM users")->fetch_assoc()['count'];

// Active Sessions (Count Confirmed appointments today)
$active_sessions_query = $conn->query("SELECT COUNT(*) as count FROM appointments WHERE status = 'Confirmed' AND appointment_date = CURDATE()");
$active_sessions = $active_sessions_query->fetch_assoc()['count'] ?? 0;

// User Growth (Count users registered per day for last 8 days)
$growth_data = [];
for ($i = 7; $i >= 0; $i--) {
    $date = date('Y-m-d', strtotime("-$i days"));
    $growth_query = $conn->prepare("SELECT COUNT(*) as count FROM users WHERE DATE(created_at) = ?");
    $growth_query->bind_param("s", $date);
    $growth_query->execute();
    $growth_data[] = (float)$growth_query->get_result()->fetch_assoc()['count'];
    $growth_query->close();
}

// Session Distribution (Dummy for now, but labels updated to recent days)
$distribution_data = [12, 15, 8, 20, 25, 10, 5];
$distribution_labels = [];
for ($i = 6; $i >= 0; $i--) {
    $distribution_labels[] = date('D', strtotime("-$i days"));
}

// Demographics (Patient, Counselor, Admin)
$demo_types = ['Patient', 'Counselor', 'Admin'];
$demo_data_counts = [];
foreach ($demo_types as $type) {
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM users WHERE user_type = ?");
    $stmt->bind_param("s", $type);
    $stmt->execute();
    $demo_data_counts[] = (float)$stmt->get_result()->fetch_assoc()['count'];
    $stmt->close();
}

echo json_encode([
    "status" => "success",
    "totalUsers" => (string)$total_users,
    "activeSessions" => (string)$active_sessions,
    "userGrowthData" => $growth_data,
    "sessionDistributionData" => $distribution_data,
    "sessionDistributionLabels" => array_values($distribution_labels),
    "userDemographicsData" => $demo_data_counts
]);

$conn->close();
?>
