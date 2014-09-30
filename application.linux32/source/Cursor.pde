class Cursor {
  float cursorRad, cursorRadMax, cursorDelta, cursorThreshold;
  
  Cursor () {
    cursorRad = 0;
    cursorRadMax = 50;
    cursorDelta = 0.5;
    cursorThreshold = 20;
  }
  
  void draw () {
    fill(255);
    noCursor();
    ellipse(mouseX, mouseY, 5, 5);
    noFill();
    
    drawRadar();
  }
  
  // draw fading radiating cicles around cursor
  void drawRadar () {
    // material red (229,28,35)
    // philo red 204,17, 17 
    stroke(229,28,35, 255-cursorRad*(255/cursorRadMax));
    if (cursorRad < cursorRadMax) {
      ellipse(mouseX, mouseY, cursorRad, cursorRad);
    }
    else if (cursorRad > (cursorRadMax + cursorThreshold)) {
      cursorRad = 5;
    }
    cursorRad = cursorRad + cursorDelta;
    noStroke();
  }
}
