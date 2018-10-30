//var btn = $("#test")
//
//btn.on("click", function(e){
//    $.ajax({
//         type : "GET",
//         url: "/helloworld",
//         contentType : "application/json",
//         success: printResponse,
//         failure: ajaxFail
//    })
//});


var btn = document.getElementById('test')
btn.addEventListener("click", function(e) {
    var request = new XMLHttpRequest();
    request.open("GET", "/helloworld", true)
    request.send();
    request.onreadystatechange = function(e){
        console.log(request.response)
    }

});