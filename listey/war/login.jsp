<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> 
<html>
<head>
  <title>Listey Login</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link rel="stylesheet" href="js/jq/jquery.mobile-1.3.1.min.css" />
  <script type="text/javascript" src="js/jq/jquery-1.10.2.min.js"></script>
  <script type="text/javascript" src="js/jq/jquery.mobile-1.3.1.js"></script>
  <script type="text/javascript" src="js/shopping.js"></script>
</head> 	
<body>
<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    String jsSnippet="";
    String site;
    if (user != null) {
      Cookie cookie = new Cookie("isLoggedIn",user.getEmail());
      cookie.setMaxAge(60*60*4);//4 hours
      response.addCookie(cookie);
      response.sendRedirect("index.html");//XXX need fully scoped somehow for phonegap?
   }//if user defined 
   else {
%>
<!-- ************************************************************************ -->
<div data-role="page" id="login-page">
  <div data-role="header">
    <h1>Welcome to Listey123!</h1>
  </div><!-- header -->

  <div data-role="content">
    <p><img src="img/logo.png" alt="Listey123 logo" style="display: block; margin-left: auto; margin-right: auto"></p>
    <p>
    This site allows you to keep lists and access them on many different
    devices.  You can log in using your GMail account.
    </p>
    <p>
    Please sign in and get started!
    </p>
    <a href="<%= userService.createLoginURL(request.getRequestURI()) %>" rel="external" data-role="button">Log In</a>
  </div><!-- content -->
</div><!-- choose-list -->
<% } %>
</body>
</html>