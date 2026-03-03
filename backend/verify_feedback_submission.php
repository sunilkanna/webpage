<?php
// Simulate a JSON POST request like Retrofit would send
$data = [
    "appointment_id" => 1,
    "patient_id" => 17,
    "rating" => 5,
    "comments" => "Excellent session! Very helpful counselor."
];

$ch = curl_init('http://localhost/genecare/submit_feedback.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "Response from submit_feedback.php:\n";
echo $response . "\n\n";

include 'db_connect.php';
$check = $conn->query("SELECT * FROM feedback WHERE appointment_id = 1 AND patient_id = 17 ORDER BY id DESC LIMIT 1");
if ($row = $check->fetch_assoc()) {
    echo "Verification Success! Data found in database:\n";
    print_r($row);
} else {
    echo "Verification Failed! No data found in database.\n";
}
?>
