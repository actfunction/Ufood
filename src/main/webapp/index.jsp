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
<script type= "text/javascript" src= "<%=urlPath %>js/invokeAjaxDemo.js"></script>
<script type= "text/javascript" src= "<%=urlPath %>js/jquery-1.8.2.min.js"></script>
<script type= "text/javascript" src= "<%=urlPath %>js/ajaxTest.js"></script>
<link rel="stylesheet" type= "text/css" href= "<%=urlPath %>css/ajaxTest.css"/>
<script type="text/javascript">

</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insert title here</title>
</head>
<body>
Hello World！
<div><span>请输入json类型的参数：</span><input id="setDataIpt" type="text" /><input onclick="btnClick();" value="确认" type="button" /></div>
<div id="setData">
	<table>
		<thead>
			<tr>
				<td class="borderClass">参数名</td>
				<td class="borderClass">参数值</td>
			</tr>
		</thead>
		<tbody id="setDataBody">
			
		</tbody>
	</table>
</div>
</body>
</html>