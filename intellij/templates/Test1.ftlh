<html>
<head>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <title>Welcome!</title>
</head>
<body>
  <h1>Welcome ${name}!</h1>
  <div id="children">
      <#list children as child>
        <p>Child ${child?counter}: ${child}</p>
      </#list>
  </div>
  <input id="newChildName" type="text"></input>
  <input id="addChild" type="button" value="Add child"></input>
  <script>
    $("#addChild").click(function() {
        var name = $("#newChildName").val();
        $.post("http://localhost:8000/test/Test1/addChild", {0: name}, function(data) {
            $("#children").append("<p>" + name + "</p>");
        });
    });
  </script>
</body>
</html>