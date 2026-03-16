<?php
// Simple JWT Generator for Jitsi JaaS (RS256)
// Reference: https://jaas.8x8.vc/basics/jwt

function generateJitsiJwt($appId, $keyId, $privateKey, $roomName, $userName, $isModerator, $userId = "") {
    $header = [
        "alg" => "RS256",
        "typ" => "JWT",
        "kid" => $keyId
    ];

    $now = time();
    $buffer = 120; // 2 minutes buffer for clock skew
    $payload = [
        "aud" => "jitsi",
        "iss" => "chat",
        "sub" => $appId,
        "room" => "*", // Wildcard room for maximum compatibility
        "iat" => $now - $buffer,
        "nbf" => $now - $buffer,
        "exp" => $now + 3600, // Token valid for 1 hour
        "context" => [
            "user" => [
                "id" => (string)$userId, // Include unique user ID
                "name" => $userName,
                "email" => "",
                "avatar" => "",
                "moderator" => $isModerator ? true : false
            ],
            "features" => [
                "livestreaming" => true,
                "recording" => true,
                "transcription" => true,
                "outbound-call" => true
            ]
        ]
    ];

    $base64UrlHeader = base64UrlEncode(json_encode($header));
    $base64UrlPayload = base64UrlEncode(json_encode($payload));

    $signature = "";
    $data = $base64UrlHeader . "." . $base64UrlPayload;
    
    // Sign using OpenSSL
    if (!openssl_sign($data, $signature, $privateKey, OPENSSL_ALGO_SHA256)) {
        return null;
    }

    $base64UrlSignature = base64UrlEncode($signature);

    return $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
}

function base64UrlEncode($data) {
    return str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($data));
}
?>
