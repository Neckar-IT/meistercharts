// Draws FontAwesome icons on the canvas once right away and once after the fonts have loaded.
const canvas = document.getElementById("myCanvas");
const gc = canvas.getContext("2d");

gc.font = "20px FontAwesome";

// Drawn before the font has loaded
gc.fillText("FontAwesome, loaded instantly", 10, 50);
gc.fillText("\uF047", 300, 50); // top

// window.onload draws only after the window, and with it the font, has loaded completely
window.onload = function () {
  gc.font = "20px FontAwesome";
  gc.fillText("FontAwesome, window.onload()", 10, 100);
  gc.fillText("\uf015", 500, 100); // middle

  gc.font = "bold 20px 'Font Awesome 6 Free'";
  gc.fillText("Font Awesome 6 Free, window.onload()", 10, 150);
  gc.fillText("\uf015", 500, 150); // bottom
};
