import ddf.minim.*;
import ddf.minim.ugens.*;
import rita.*;

AudioGenerator synth;
Flock flock, flockB;
Cursor cursor;
Corpus corpus;

boolean showScroll = false;
boolean overScrollbar = false;

void setup() {
  size(displayWidth, displayHeight);
  if (frame != null) {
    frame.setResizable(true);
  }
  smooth();
  setupScrollbars();
  
  synth = new AudioGenerator();
  cursor = new Cursor();
  corpus = new Corpus("article.txt", "filtertokens.txt");
  
  String line = corpus.getLine(corpus.lines);

  int num = int(random(3, 7));
  flock = new Flock();
  // Add an initial set of boids into the system
  for (int i = 0; i < num; i++) {
    flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, corpus.getToken(line)));
   }
}

void draw() {
  background(20,20,20);  
  cursor.draw();
  flock.run();
  drawScrollbars();
}

void clear () {
  flock.clear();
}

void mouseClicked() {
  flock.addBoid(new Boid(mouseX,mouseY, flock.boids, corpus.newToken()));
}

void mousePressed(){
  flock.mousePressed();
}

void mouseReleased(){
  flock.mouseReleased();
}

void keyPressed() {  
  if (keyCode == ENTER || keyCode == RETURN) {
    
  }
  else if (keyCode == ' ') {
    if(flock.boids.size() > 0){
      clear();
    }
    else {
      int num = int(random(3, 7));
      String line = corpus.getLine(corpus.lines);
      for (int i = 0; i < num; i++) {    
        flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, corpus.getToken(line)));
      }
    }
  }
  else if (keyCode == 'S'){
    println("S");
    showScroll = !showScroll;
  }
}
