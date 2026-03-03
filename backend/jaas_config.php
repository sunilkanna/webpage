<?php
// Jitsi as a Service (JaaS) Configuration
// Get these from https://8x8.vc/

// Your JaaS App ID (Tenant ID)
define('JAAS_APP_ID', 'vpaas-magic-cookie-2b60b72ee4404d33bc70c84652835e3a');

// Your Private Key (The content of the .pk file you download from 8x8)
// Make sure to include the BEGIN and END PRIVATE KEY lines.
$jaas_private_key = <<<EOD
-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCCxijQY39cbemn
rm1wGr2AyeGHbLf2KRM4ImqT882+yDQuW+BxMDVaTCM8p6J8Vd50MmU9WmGO1R0I
MJz0Kfgqw5wapgGRFIbjuQoXpsqZqDy4//J4ItUWjF9HleQYjbewiHhFr7i7vWRg
tnpIV2QYU0Lq5ZYR8gHrZDH++WFRbJg6ag2aw/HnOR4fMuVGtQCHZPNlKENlGJnG
79/aLynw/Kl21WD7DB1ZGcUwtt/Vj26mp6TGhIrt3mebjJ9GqQRc1JOj3ok29sxq
yLw6AaELjawG4HN1y/5JrEGNKzADytQfJfO45b9xtIhXgOqKn/vX9IX/lxGkna9u
8k7YMVX7AgMBAAECggEAH0c8HdOec8IKP8P1gvmyV6pxmc2EFolugl+g9DA9BqOl
72ykOw5XxHgveWNy9qAnK82d71W9vcb1Qa826yNaqwfMP4dzGns5ZCAu7MdE8AmQ
NINrQBa8tr7uTxXnz4Od6shJVAmaerTq3ELo7oKQe0z21W4CPdqPYPi/CIgnIa0p
aGv1w4mfCYIw8/mxMvgytYbnVjbQbVQVSSYRmVZuU0lRHVZYoHiRQY9wC7wafhUe
mK/yfhRePCUh70QQ5XNeB5BT0SNxhz6+lq9IerCtX1IKKHld+Qqvlgf6IVuKSR4U
wxqc1yA9h+/AKxnOdVY3gHU9CSEisZ5siMhj/Uzh8QKBgQDeopP810voY/2n4pf3
MKj3TxXvJxxlpvkKCdgJBoGvBYqLaJJVWnf7A/21su7nfwC7fgcKu9Ve11v7o+Ed
XLRs74AUo5Ls0mcPX37qiOhzYbFmR12s47CyiIdpva9C1/Pb0an1ScPq5JJsGXCp
R3N1pbWeFwSO2RiAVpcWU/nawwKBgQCWX1KG0Uurt6MYdQmCZnERxBT9/EJ4E0TF
f7qfKWLZgF1JsyO88aVvEr4Pu8YjH82uJeQdO/FsyTBi2QxFN3pXMgo+7JQlRjJC
H9oj10NYjPqsoTDIdWJ5yCx3oJ4MnY03v5q7Kv6sdDkFpEjFGy7MBQ9fWlHwKAVz
cKnL5i00aQKBgHyU21z0GRz8vxdwG/uN9i8VyT9fUmurqNYuNf6u7mjDIAjsxl4O
1u7XB+TiMP/HuOAyILyn6Tk+J9HDnZfxpWEEEFb8RZ5SCqu2k22omPLf6wZBzzCM
5PayoZvRMj6kW2hZhdKlqYVKzIjDH6EKQ5jE1sGVPc61uxkt2zP6X+lDAoGAXmKW
umjExpQNm1xbC66hajVZt9KPJi9yC0WRLjYcBcm8xHXdCMvku0NYaSfDxmhAAf9m
n7LwZwghhuoV+ZxI/lI6fCSjGIJnKXo0wipr6zkSXykOJcUIeshaoPwAOIrsCQIL
bVAfar/IyvHDe3UEeqryazeWFtgIE92ztnhUB4ECgYEA2e1/KtRrFUSi7YfFsncJ
pKL1TQiuPN/vUxZ4p4Q4sPK1Sl7hXGCIxpGoq2fJKvtwVJjxswMWAyoGaw5nQzXk
NULChswgd8aZLbuhKlSvc7f/p8+7tncS9dPsI+7s68TH9jNOgfMNZqjEY9FuBkoG
XO2TW2fPYB07VEQ+a8U8JK0=
-----END PRIVATE KEY-----
EOD;

define('JAAS_PRIVATE_KEY', $jaas_private_key);

// Key ID (The ID of the API key you created in the 8x8 console)
define('JAAS_KEY_ID', 'vpaas-magic-cookie-2b60b72ee4404d33bc70c84652835e3a/ee08e8');
?>
