<html>
<head>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <title>Welcome!</title>
</head>
<body>
  <h1>Here are the available procedures:</h1>
  <div id="procedures">
      <#list procedures as procedure>
        <div><a href="${procedure.url}">Procedure ${procedure?counter}: ${procedure.name}</a></div>
      </#list>
  </div>
  <div id="startedProcedures">
      <#list startedProcedures as startedProcedure>
        <p>${startedProcedure}</p>
      </#list>
  </div>
</body>
</html>