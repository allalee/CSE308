var sideNavState = 0;
function toggleNav(){
  if (sideNavState===0) { //Open nav
    document.getElementById("sidenav").style.width = "300px";
    document.getElementById("main").style.marginLeft = "300px";
    //document.getElementById("controls").style.visibility = "visible";
    sideNavState = 1;
  } else { //Close nav
    document.getElementById("sidenav").style.width = "0";
    document.getElementById("main").style.marginLeft= "0";
    //document.getElementById("controls").style.visibility = "hidden";
    //document.getElementById("home").setAttribute("class", "collapse");
    //document.getElementById("events").setAttribute("class", "collapse");
    sideNavState = 0;
  }
}
var slider = document.getElementById("customRange3");
function getValueEx() {
  var val = document.getElementById("customRange3").value;
  console.log(val);
}




// remove all below when DB is done
function color_neighbors( precinct_id ){
    $.ajax({
        type : "GET",
        url: "/getNeighbor?id="+precinct_id,
        contentType : "application/json",
        success: function(res){
            console.log(res);
            $.parseJSON(res).forEach(function(e){
                color_district(e, "red")
            });
        },
        failure: function(e){console.log("get neighbor failed");}
    })
}

function serverLoadKansas(){
    console.log("please wait while we set up Kansas")
    $.ajax({
        type : "GET",
        url: "/loadKansas",
        contentType : "application/json",
        success: function(e){console.log(e)},
        failure: function(e){console.log("kansas failed");}
    })
}
serverLoadKansas();

$('#start').on("click", function(e){
    $.ajax({
        type : "GET",
        url: "/startAlgorithm?state_name=Kansas_2",
        contentType : "application/json",
        success: function(e){console.log(e)},
        failure: function(e){console.log("Algo failed to start");}
    })
});
