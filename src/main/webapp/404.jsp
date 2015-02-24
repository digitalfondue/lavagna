<!doctype html>
<html data-ng-app="lavagna">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title data-ng-bind="pageTitle" data-translate>404</title>
<!--  see ResourceController -->
<link rel="stylesheet" href="css/all.css" type="text/css">
<!--  -->
</head>
<body>
	<script type="text/javascript">
		window.location = "<%= request.getContextPath() %>/not-found";
	</script>
</body>
</html>