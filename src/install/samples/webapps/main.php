<?php
session_start ();
if ( isset ( $_SESSION['username'])) {
	header ( "location:login_success.php" );
}

//detect the locale, get the resource bundle
$language='';
$supportedLangs = array('zh', 'en', 'fr', 'de');
$languages = explode(',',$_SERVER['HTTP_ACCEPT_LANGUAGE']);
foreach($languages as $lang)
{
    foreach($supportedLangs as $supportedLang)
	{
        // Set the page locale to the first supported language found
		//setLocale(LC_ALL, $lang);
		if(substr(strtolower($lang),0,2) === $supportedLang){
			$language = $supportedLang;
			break;
		}
	}
}

$property_file = "properties_" . $language . ".ini";
// Parse .ini property with sections
if(!file_exists($property_file)){
	$property_file = "properties.ini";
}
$_SESSION['properties'] = parse_ini_file($property_file, true);

?>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<div>
			<table width="300" border="0" align="center" cellpadding="0"
				cellspacing="1" bgcolor="#CCCCCC">
				<tr>
					<form name="form1" method="post" action="checklogin.php">
						<td>
							<table id="loginForm" width="100%" border="0" cellpadding="3" cellspacing="1"
								bgcolor="#FFFFFF">
								<tr>
									<td colspan="3"><strong>Member Login </strong></td>
								</tr>
								<tr>
									<td width="78">Username</td>
									<td width="6">:</td>
									<td width="294"><input name="myusername" type="text"
										id="myusername"></td>
								</tr>
								<tr>
									<td>Password</td>
									<td>:</td>
									<td><input name="Passwd" type="text" placeholder="Password" id="Passwd"></td>
								</tr>
								<tr>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
									<td><input type="submit" name="Login" value="<?php echo $_SESSION['properties']['main']['LoginLabel']?>"></td>
								</tr>
								<tr>
									<td colspan=3>
										<?php 
											if ( isset ( $_SESSION['errorMsg'])) {
												echo "<font color='red' id='errorMsg'>" . $_SESSION['errorMsg'] . "</font>";
											}
										?>
									</td>
								</tr>
							</table>
						</td>
					</form>
				</tr>

			</table>

      </div> 
      
   </body>
</html>