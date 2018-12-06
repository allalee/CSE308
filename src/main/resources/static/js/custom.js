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
var pop_slider = document.getElementById("population_equality");
var pop_output = document.getElementById("pop_value");
pop_output.innerHTML = pop_slider.value;

pop_slider.oninput = function() {
  pop_output.innerHTML = this.value;
}

var part_slider = document.getElementById("partisan_fairness");
var part_output = document.getElementById("part_value");
part_output.innerHTML = part_slider.value;

part_slider.oninput = function() {
  part_output.innerHTML = this.value;
}

var comp_slider = document.getElementById("compactness");
var comp_output = document.getElementById("comp_value");
comp_output.innerHTML = comp_slider.value;

comp_slider.oninput = function() {
  comp_output.innerHTML = this.value;
}

function select_map(e) {
  selected = e.innerText
  target = document.getElementById("dropdownMapButton")
  target.innerText = selected
}
