var sideNavState = 0;
function toggleNav(){
  if (sideNavState===0) { //Open nav
    document.getElementById("sidenav").style.width = "300px";
    document.getElementById("main").style.marginLeft = "300px";
    sideNavState = 1;
  } else {
    document.getElementById("sidenav").style.width = "0";
    document.getElementById("main").style.marginLeft= "0";
    sideNavState = 0;
  }
}
var slider = document.getElementById("customRange3");
function getValueEx() {
  var val = document.getElementById("customRange3").value;
  console.log(val);
}