<?php
/*
 * Settings of the confirmation page. Copy this file to config.php (same
 * folder) and fill it in. config.php is not part of the repository.
 *
 * The database account only needs to read the codes and to leave a message
 * for services. INSTALL, section "The confirmation page", has the grants.
 */
return [
    /* The name of the network, shown on the page */
    'network' => 'ExampleNet',

    /* A link back to your site at the bottom of the page, '' for none */
    'home'    => 'https://example.net/',

    /* The database of Avade */
    'db_host' => '127.0.0.1',
    'db_port' => 3306,
    'db_name' => 'avade',
    'db_user' => 'avadeweb',
    'db_pass' => 'CHANGE-THIS',
];
