<%@page import="com.fe.ufood.bean.MessageBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<%
	String path = request.getContextPath();
	String urlPath = request.getScheme() + "://"
	    + request.getServerName() + ":" + request.getServerPort()
	    + path + "/";
%>
<script type="text/javascript">
var FireFlyContextPath = "<%=urlPath %>";//虚拟路径
</script>
<script type= "text/javascript" src= "<%=urlPath %>js/invokeAjaxDemo.js"></script >
<script type= "text/javascript" src= "<%=urlPath %>js/jquery-1.8.2.min.js"></script >
<script type="text/javascript">
var param = {"a":1, "b":2, "c":3};
FireFly.doAct("httpAgent", "agent", param, "", true);
</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insert title here</title>
</head>
<body>
Hello Jack Li ！
</body>
</html>