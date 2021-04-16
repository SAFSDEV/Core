<?php
	session_start ();
	unset($_SESSION['errorMsg']);
	
	// username and password sent from form
	$myusername = $_POST ['myusername'];
	$mypassword = $_POST ['Passwd'];
	
	$myusername = stripslashes ( $myusername );
	$mypassword = stripslashes ( $mypassword );
	
	// If result matched $myusername and $mypassword
	if ($myusername == 'tom' && $mypassword== '1234') {
		// Register $myusername, $mypassword and redirect to file "login_success.php"
		$_SESSION['username'] = $myusername;
		header ( "location:login_success.php" );
	} else {
		$_SESSION['errorMsg'] = 'Wrong Username or Password';
		header ( "location:main.php" );
	}
?>