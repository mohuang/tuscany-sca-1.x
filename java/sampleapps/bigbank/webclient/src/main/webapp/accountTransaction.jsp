<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%--
  Copyright (c) 2005 The Apache Software Foundation or its licensors, as applicable.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 --%>

<HTML>
<HEAD>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<META name="GENERATOR" content="IBM Software Development Platform">
<META http-equiv="Content-Style-Type" content="text/css">
<LINK href="theme/Master.css" rel="stylesheet" type="text/css">
<TITLE>BigBank - <%=request.getParameter("account") %> </TITLE>
</HEAD>
<BODY>
<P>Account <%=  request.getParameter("account") %><BR>
<BR>
<BR>
</P>
<FORM method="post" action="FormServlet">
<input type="hidden" name="action"    value='account' />
<input type="hidden" name="account"    value='<%=  request.getParameter("account") %>' />
<input type="hidden" name="actionType"    value='<%=request.getParameter("transaction")%>' />
Amount to <%=request.getParameter("transaction")%> <INPUT type="text" name="Amount" size="10"
	maxlength="10"><BR>
<BR>
<BR>
<BR>
<INPUT type="submit" name="Submit"
	value="Submit">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type="submit"
	name="cancel" value="cancel">
</FORM>
</BODY>
</HTML>
