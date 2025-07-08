<%@page import="com.zipstory.board.model.BoardVO"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>Board List</title>
</head>
<body>
<h1>게시글 목록</h1>
<div id="board-list"></div>

<script>
	// API 호출 (AJAX)
	fetch('/api/boards')
			.then(response => response.json())
			.then(data => {
				const container = document.getElementById('board-list');
				data.forEach(board => {
					const div = document.createElement('div');
					div.innerText = `[${board.id}] ${board.title}`;
					container.appendChild(div);
				});
			});
</script>
</body>
</html>
