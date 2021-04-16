<?php
session_start ();
if (! isset ( $_SESSION['username'])) {
	header ( "location:main.php" );
}
?>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<div id="successMsg">Login Successful, welcome <?php echo $_SESSION['username']?></div>
		<form class = "form-signin" role = "form" action = "logout.php" method = "post">
	        <button type = "submit" name = "logout"><?php echo $_SESSION['properties']['login_success']['LogoutLabel']?></button>
	    </form>
	</body>
</html>