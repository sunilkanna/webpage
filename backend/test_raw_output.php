<?php
$url = 'http://localhost/genecare/start_session.php';
$data = ['appointment_id' => 65, 'user_id' => 1013];

$options = [
    'http' => [
        'header'  => "Content-type: application/json\r\n",
        'method'  => 'POST',
        'content' => json_encode($data),
        'ignore_errors' => true
    ]
];

$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

echo "RESPONSE_START\n";
var_dump($result);
echo "\nRESPONSE_END";
?>
