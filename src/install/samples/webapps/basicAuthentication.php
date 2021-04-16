<?php
$needAuth = true;

if (isset($_SERVER['PHP_AUTH_USER'])) {
	if ($_SERVER['PHP_AUTH_USER']=='tom' && $_SERVER['PHP_AUTH_PW']=='hello'){
		echo "<p id='welcomeMsg'>Welcome {$_SERVER['PHP_AUTH_USER']}, you have logged on.</p>";
		$needAuth = false;
	}else{
		echo "<p>Hello {$_SERVER['PHP_AUTH_USER']}.</p>";
		echo "<p>Username or password is wrong.</p>";	
	}
}

//echo "needAuth= {$needAuth}";

if($needAuth){
    header('WWW-Authenticate: Basic realm="My Realm"');
    header('HTTP/1.0 401 Unauthorized');
    echo 'You abandonned LogOn.';
    exit;	
}

?>