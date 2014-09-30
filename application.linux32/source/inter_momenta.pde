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
  corpus = new Corpus("data/article.txt", "data/filtertokens.txt");
  
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

void newMomentum(){
  int num = int(random(3, 7));
  for (int i = 0; i < num; i++) {    
    flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, corpus.newToken()));
  }
}


void togglePause () {
  flock.togglePause();
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
    if(flock.boids.size() > 0){
      clear();
    }
    else {
      newMomentum();
    }
  }
  else if (keyCode == ' ') {
    togglePause();
  }
  else if (keyCode == 'S'){
    // scrollbar settings
    showScroll = !showScroll;
  }
  else if (keyCode == 'N'){
    newMomentum();
  }
  else if (keyCode == 'D'){
    // demo
    String[] tokens = {"coniunctus","inter","momenta"};
    for (int i = 0; i < tokens.length; i++) {
      flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, tokens[i]));
    }
  }
}
