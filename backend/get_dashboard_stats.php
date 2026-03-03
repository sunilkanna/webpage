<?php
include_once 'db_connect.php';

// Handle JSON input
$data = json_decode(file_get_contents("php://input"), true) ?? [];

$user_id = $_POST['user_id'] ?? $data['user_id'] ?? null;
$user_type = $_POST['user_type'] ?? $data['user_type'] ?? null;

if (empty($user_id) || empty($user_type)) {
    echo json_encode(["status" => "error", "message" => "User ID and type are required"]);
    exit();
}

$response = [
    "status" => "success",
    "patient_stats" => null,
    "counselor_stats" => null
];

if ($user_type === 'Patient') {
    // 1. Get Risk Assessment
    $risk_stmt = $conn->prepare("SELECT risk_score, risk_category, assessed_at FROM risk_assessments WHERE patient_id = ? ORDER BY assessed_at DESC LIMIT 1");
    $risk_stmt->bind_param("i", $user_id);
    $risk_stmt->execute();
    $risk_result = $risk_stmt->get_result();
    $risk_data = $risk_result->fetch_assoc();
    
    // 2. Get Next Appointment
    $app_stmt = $conn->prepare("SELECT a.*, u.full_name as counselor_name 
                               FROM appointments a 
                               JOIN users u ON a.counselor_id = u.id 
                               WHERE a.patient_id = ? AND a.appointment_date >= CURDATE() AND a.status != 'Cancelled' 
                               ORDER BY a.appointment_date ASC, a.time_slot ASC LIMIT 1");
    $app_stmt->bind_param("i", $user_id);
    $app_stmt->execute();
    $app_result = $app_stmt->get_result();
    $app_data = $app_result->fetch_assoc();

    $response["patient_stats"] = [
        "risk_score" => $risk_data['risk_score'] ?? 0,
        "risk_category" => $risk_data['risk_category'] ?? "Not Assessed",
        "last_assessment_date" => $risk_data['assessed_at'] ?? null,
        "upcoming_appointment" => $app_data
    ];

} else if ($user_type === 'Counselor') {
    // 1. Today's Sessions Count
    $today_stmt = $conn->prepare("SELECT COUNT(*) as count FROM appointments WHERE counselor_id = ? AND appointment_date = CURDATE() AND status != 'Cancelled'");
    $today_stmt->bind_param("i", $user_id);
    $today_stmt->execute();
    $today_count = $today_stmt->get_result()->fetch_assoc()['count'];

    // 2. Total Unique Patients
    $total_stmt = $conn->prepare("SELECT COUNT(DISTINCT patient_id) as count FROM appointments WHERE counselor_id = ?");
    $total_stmt->bind_param("i", $user_id);
    $total_stmt->execute();
    $total_patients = $total_stmt->get_result()->fetch_assoc()['count'];

    // 3. Today's Appointment List
    $list_stmt = $conn->prepare("SELECT a.*, u.full_name as patient_name 
                                FROM appointments a 
                                JOIN users u ON a.patient_id = u.id 
                                WHERE a.counselor_id = ? AND a.appointment_date = CURDATE() AND a.status != 'Cancelled' 
                                ORDER BY a.time_slot ASC");
    $list_stmt->bind_param("i", $user_id);
    $list_stmt->execute();
    $list_result = $list_stmt->get_result();
    $appointments = [];
    while ($row = $list_result->fetch_assoc()) {
        $appointments[] = $row;
    }

    // 4. Calculate Revenue This Month
    $rev_stmt = $conn->prepare("
        SELECT SUM(p.amount) as total 
        FROM payments p 
        JOIN appointments a ON p.appointment_id = a.id 
        WHERE a.counselor_id = ? 
        AND p.status = 'Completed' 
        AND MONTH(p.created_at) = MONTH(CURRENT_DATE()) 
        AND YEAR(p.created_at) = YEAR(CURRENT_DATE())
    ");
    $rev_stmt->bind_param("i", $user_id);
    $rev_stmt->execute();
    $revenue_data = $rev_stmt->get_result()->fetch_assoc();
    $revenue = $revenue_data['total'] ?? 0;
    
    $revenue_formatted = "₹" . number_format($revenue);

    // 5. Avg Rating
    $rate_stmt = $conn->prepare("SELECT AVG(f.rating) as avg_rating FROM feedback f JOIN appointments a ON f.appointment_id = a.id WHERE a.counselor_id = ?");
    $rate_stmt->bind_param("i", $user_id);
    $rate_stmt->execute();
    $avg_rating = $rate_stmt->get_result()->fetch_assoc()['avg_rating'] ?? 0;
    $avg_rating = round($avg_rating, 1);

    // 5.1 Pending Requests Count
    $pending_stmt = $conn->prepare("SELECT COUNT(*) as count FROM appointments WHERE counselor_id = ? AND status = 'Pending'");
    $pending_stmt->bind_param("i", $user_id);
    $pending_stmt->execute();
    $pending_count = $pending_stmt->get_result()->fetch_assoc()['count'];


    // 6. Recent Reviews
    $reviews_stmt = $conn->prepare("SELECT f.rating, f.comments as review, f.submitted_at, u.full_name as author 
                                   FROM feedback f 
                                   JOIN appointments a ON f.appointment_id = a.id 
                                   JOIN users u ON f.patient_id = u.id
                                   WHERE a.counselor_id = ? 
                                   ORDER BY f.submitted_at DESC LIMIT 3");
    $reviews_stmt->bind_param("i", $user_id);
    $reviews_stmt->execute();
    $reviews_result = $reviews_stmt->get_result();
    $recent_reviews = [];
    while ($row = $reviews_result->fetch_assoc()) {
        $row['days_ago'] = floor((time() - strtotime($row['submitted_at'])) / (60 * 60 * 24)) . " days ago";
        $recent_reviews[] = $row;
    }

    $response["counselor_stats"] = [
        "todays_sessions" => (int)$today_count,
        "total_patients" => (int)$total_patients,
        "pending_requests_count" => (int)$pending_count,
        "avg_rating" => (double)$avg_rating,
        "revenue_this_month" => $revenue_formatted,
        "today_appointments" => $appointments,
        "recent_reviews" => $recent_reviews
    ];
}

echo json_encode($response);

$conn->close();
?>
