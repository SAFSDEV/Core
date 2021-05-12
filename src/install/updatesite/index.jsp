<!-- This jsp page is used to list all update assets ( .zip .jar files) under the root path -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.FilenameFilter, java.io.File, java.lang.String " %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Latest Update Assets</title>
</head>
<body>
	<h1>Latest Update Assets</h1>
	<ul>
	<%	    
		//String root="C:\\Apache\\Tomcat7.0\\webapps\\updatesite";
		String root = request.getRealPath("/");
		java.io.File file;
		java.io.File dir = new java.io.File(root);
	
		String[] list = dir.list(
				new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						String lcname = name.toLowerCase().trim();
					    return lcname.endsWith(".zip") || lcname.endsWith(".jar");
					}
				}
			);
	
		if (list.length > 0) {
		  for (int i = 0; i < list.length; i++) {%>
			<li><a href="<%=list[i]%>" target="_top"><%=list[i]%></a><br>
	<%
		  }
		}
	%>
	</ul>
</body>
</html>