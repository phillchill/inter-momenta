import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.ugens.*; 
import rita.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class inter_momenta extends PApplet {





AudioGenerator synth;
Flock flock, flockB;
Cursor cursor;
Corpus corpus;

boolean showScroll = false;
boolean overScrollbar = false;

public void setup() {
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

  int num = PApplet.parseInt(random(3, 7));
  flock = new Flock();
  // Add an initial set of boids into the system
  for (int i = 0; i < num; i++) {
    flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, corpus.getToken(line)));
   }
}

public void draw() {
  background(20,20,20);  
  cursor.draw();
  flock.run();
  drawScrollbars();
}

public void newMomentum(){
  int num = PApplet.parseInt(random(3, 7));
  for (int i = 0; i < num; i++) {    
    flock.addBoid(new Boid(width/2+random(-10, 10),height/2+random(-10, 10), flock.boids, corpus.newToken()));
  }
}


public void togglePause () {
  flock.togglePause();
}

public void clear () {
  flock.clear();
}

public void mouseClicked() {
  flock.addBoid(new Boid(mouseX,mouseY, flock.boids, corpus.newToken()));
}

public void mousePressed(){
  flock.mousePressed();
}

public void mouseReleased(){
  flock.mouseReleased();
}

public void keyPressed() {  
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
class AudioGenerator {
  Minim minim;
  AudioOutput out;

  // Constructor initialize all values
  AudioGenerator() {
    minim = new Minim(this);
    // use the getLineOut method of the Minim object to get an AudioOutput object
    out = minim.getLineOut();
  }
}
// Inspired by
// Daniel Shiffman <http://www.shiffman.net>
// The Nature of Code, 2011

// Boid class

float swt;
float cwt;
float awt;
float maxspeed = 1;
float maxforce = 0.025f;

class Boid {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float gazeRad;
  float r;
  
  float hoverRad;
  boolean dragging = false;
  
  int lifespan;
  int leftToLive;
  int birth;
  
  boolean wrapAround;
  ArrayList<Boid> nbs;
  
  String word;
  float txtSize;
  
  float radialEasing = 0.03f;
  float reactionRadial = 0;
  float reactionRadialMax = 100;
  
  float surfaceEasing = 0.3f;
  float textSize = 0;
  float textSizeMax = 20;

  // Constructor initialize all values
  Boid(float x, float y, ArrayList<Boid> peers, String token) {
    location = new PVector(x, y);
    maxspeed = 1.5f;
    maxforce = 0.25f;
    acceleration = new PVector(0, 0);
    velocity = new PVector(random(-0.5f*maxspeed,0.5f*maxspeed), random(-0.5f*maxspeed,0.5f*maxspeed));
    r = 3;
    gazeRad = 100;
    wrapAround = false;
    
    lifespan = 160000;
    leftToLive = lifespan;
    birth = millis();
    
    word = token;
    txtSize = 20 + random(-10, 10);
    
    // connect 2 neighbours
    connect2Neighbours(peers);
  }
  
  public void connect2Neighbours(ArrayList<Boid> peers){
    nbs = new ArrayList<Boid>();
    // For every peer boid, check if it's in the gaze radius
    for (Boid other : peers) {
      float d = PVector.dist(location, other.location);
      if(d > 0 && d < gazeRad){
        nbs.add(other);
      }
    }
    synth.out.playNote(0, .5f, 50+nbs.size()*100);
  }
  
  public void applyForce(PVector force) {
    // We could add mass here if we want A = F / M
    acceleration.add(force);
  }
  
  // Separation
  // Method checks for nearby vehicles and calculates steer away force
  public PVector separate (ArrayList<Boid> vehicles) {
    float desiredseparation = r*15;
    PVector steer = new PVector(0,0);
    int count = 0;
    // For every boid in the system, check if it's too close
    for (Boid other : vehicles) {
      float d = PVector.dist(location, other.location);
      // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
      if ((d > 0) && (d < desiredseparation)) {
        // Calculate vector pointing away from neighbor
        PVector diff = PVector.sub(location, other.location);
        diff.normalize();
        diff.div(d);        // Weight by distance
        steer.add(diff);
        count++;            // Keep track of how many
      }
    }
    // Average -- divide by how many
    if (count > 0) {
      steer.div((float)count);
      // Our desired vector is the average scaled to maximum speed
      steer.normalize();
      steer.mult(maxspeed);
      // Implement Reynolds: Steering = Desired - Velocity
      steer.sub(velocity);
      steer.limit(maxforce);
    }
    return steer;
  }
  
  // Cohesion
  // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
  public PVector cohesion (ArrayList<Boid> boids) {
    float neighbordist = 90.0f;
    PVector steer = new PVector(0,0);   // Start with empty vector to accumulate all locations
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(location,other.location);
      if ((d > 0) && (d < neighbordist)) {
        steer.add(other.location); // Add location
        count++;
      }
    }
    if (count > 0) {
      steer.div((float)count);
      return seek(steer);  // Steer towards the location
    }
    return steer;
  }
  
  // Alignment
  // For every nearby boid in the system, calculate the average velocity
  public PVector align (ArrayList<Boid> boids) {
    float neighbordist = 50.0f;
    PVector steer = new PVector();
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(location,other.location);
      if ((d > 0) && (d < neighbordist)) {
        steer.add(other.velocity);
        count++;
      }
    }
    if (count > 0) {
      steer.div((float)count);
      // Implement Reynolds: Steering = Desired - Velocity
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(velocity);
      steer.limit(maxforce);
    }
    return steer;
  }
  
  // A method that calculates and applies a steering force towards a target
  // STEER = DESIRED MINUS VELOCITY
  public PVector seek(PVector target) {
    PVector desired = PVector.sub(target,location);  // A vector pointing from the location to the target

    // Normalize desired and scale to maximum speed
    desired.normalize();
    desired.mult(maxspeed);
    // Steering = Desired minus Velocity
    PVector steer = PVector.sub(desired,velocity);
    steer.limit(maxforce);  // Limit to maximum steering force

    return steer;
  }

  public void run(ArrayList<Boid> boids) {
    flock(boids);
    update();
    // pull();
    mouse();
    borders();
    display();
  }
  
  public void flock (ArrayList<Boid> boids) {
    PVector sep = separate(boids);   // Separation
    PVector ali = align(boids);      // Alignment
    PVector coh = cohesion(boids);   // Cohesion
    // weight these forces
    
    sep.mult(swt);
    ali.mult(awt);
    coh.mult(cwt);
    // Add the force vectors to acceleration
    applyForce(sep);
    applyForce(ali);
    applyForce(coh);
  }

  // Method to update location
  public void update() {
    
    // move
    // Update velocity
    velocity.add(acceleration);
    // Limit speed
    velocity.limit(maxspeed);
    location.add(velocity);
    // Reset accelertion to 0 each cycle
    acceleration.mult(0);
    
    // decay life
    leftToLive = lifespan - (millis() - birth);
  }
  
  public void mouse() {
    if(dragging){
      location.x = mouseX;
      location.y = mouseY;
    }
  }

  public void display() {
    /// appear on creation
    // radial touch
    float dRadial = reactionRadialMax - reactionRadial;
    if(dRadial != 0){
      noStroke();
      reactionRadial += abs(dRadial) * radialEasing;
      fill(255,255,255,150-reactionRadial*(255/reactionRadialMax));
      ellipse(location.x, location.y, reactionRadial, reactionRadial);
    }
    
    // grow text from point of origin
    float dText = textSizeMax - textSize;
    if(dText != 0){
      noStroke();
      textSize += abs(dText) * surfaceEasing;
      textSize(textSize);
    }
    
    // draw agent (point)
    fill(255);
    pushMatrix();
    translate(location.x, location.y);
    float r = 3;
    ellipse(0, 0, r, r);
    popMatrix();
    noFill();    
    
    // draw text 
    textSize(txtSize);
    float lifeline = map(leftToLive, 0, lifespan, 25, 255);
    if(mouseOver()){
      stroke(229,28,35);
      fill(229,28,35);
    }
    else {
      stroke(255);
      fill(255);
    }
    
    text(word, location.x, location.y);
    
    // draw connections to neighboars
    gaze();
  }
  
  public void gaze() {
    // For every neighbour, draw line
    float lifeline = map(leftToLive, 0, lifespan, 1, 255);
    stroke(255,255,255,lifeline);
    
    Iterator<Boid> it = nbs.iterator();
    while (it.hasNext()) {
      Boid other = it.next();
      if (other.isDead()) {
         it.remove();
      }
      else {
        line(location.x, location.y, other.location.x, other.location.y);
      }
    }
      
    // draw gaze circle
    noStroke();
    fill(204,17, 17, 40);
    float mouseDist = PVector.dist(location, new PVector(mouseX,mouseY));
    if(mouseDist < gazeRad) {
//      ellipse(location.x, location.y, 2*gazeRad, 2*gazeRad);
    }    
  }

  
  public void borders() {
    if(wrapAround) {
      // Wraparound
      if (location.x < -r) location.x = width+r;
      if (location.y < -r) location.y = height+r;
      if (location.x > width+r) location.x = -r;
      if (location.y > height+r) location.y = -r;    
    }
    else {
      // bounce
      if (location.x > width-r || location.x < r) {
        velocity.x *= -1;
      }
      if (location.y > height-r || location.y < r) {
        velocity.y *= -1;
      }
    }
  }
  
  public void togglePause () {
    paused = !paused;
  }
  
  public boolean isDead(){
    return !(leftToLive > 0);
  }
  
  public boolean mouseOver(){
    float mouseDist = PVector.dist(location, new PVector(mouseX,mouseY));
    return mouseDist < 30;
  }

}
class Corpus {
  RiMarkov markov;
  Random r = new Random();
  ArrayList<String> lines;
  String corpusPath;
  ArrayList<String> filterTokens;
  String filterPath;
  
  Corpus (String corpusPath, String filterPath) {
    lines = loadLines(corpusPath);
    filterTokens = loadLines(filterPath);    
  }
  
  // load lines from filename, skipping empty lines
  public ArrayList<String> loadLines(String filename) {
    ArrayList<String> lines = new ArrayList<String>();
    String[] rawLines = loadStrings(filename);
    for(String str : rawLines) {
      if(str != null && !str.isEmpty()){
        lines.add(str);
      }
    }
    return lines;
  }
  
  public String getLine(ArrayList<String> lines) {
    String line = "";
    while(line.isEmpty()){
      line = lines.get(r.nextInt(lines.size()));  
    }
    return line;
  }
  
  // pick randomm filtered token from specific line
  public String getToken(String line){  
    String[] tokensArr = splitTokens(line, " &,+.'`");
    ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(tokensArr));
    
    // filter tokens
    tokens = filterTokens(tokens);
    String token = tokens.get(r.nextInt(tokens.size()));
    return token;
  }
  
  public ArrayList<String> filterTokens (ArrayList<String> tokens) {
    Iterator<String> it = tokens.iterator();
    while (it.hasNext()) {
      String token = it.next();
      if (filterTokens.contains(token.toLowerCase()) || token.length() < 2 ){
        it.remove();
      }
    }
    return tokens;
  }
  
  // pick random token from global lines
  public String newToken(){
    String line =  getLine(lines);
    String token = getToken(line);
    return token;
  }
}
class Cursor {
  float cursorRad, cursorRadMax, cursorDelta, cursorThreshold;
  
  Cursor () {
    cursorRad = 0;
    cursorRadMax = 50;
    cursorDelta = 0.5f;
    cursorThreshold = 20;
  }
  
  public void draw () {
    fill(255);
    noCursor();
    ellipse(mouseX, mouseY, 5, 5);
    noFill();
    
    drawRadar();
  }
  
  // draw fading radiating cicles around cursor
  public void drawRadar () {
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
// Flocking
// Daniel Shiffman <http://www.shiffman.net>
// The Nature of Code, Spring 2011

boolean paused;

class Flock {
  ArrayList<Boid> boids; // An ArrayList for all the boids

  Flock() {
    boids = new ArrayList<Boid>(); // Initialize the ArrayList
  }

  public void run() {
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      if(!paused){
        if (b.isDead()) {
         it.remove();
        }
        else {
          b.run(boids);
        }
      }
      else { //paused
        b.mouse();
        b.display();
      }
      
    }
  }

  public void addBoid(Boid b) {
    boids.add(b);
  }
  
  public void clear() {
    boids.clear();
  }
  
  public void togglePause () {
    paused = !paused;
  }
  
  public void mousePressed(){
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      if (b.mouseOver()) {
        b.dragging = true;
      }
    }  
  }
  
  public void mouseReleased(){
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      b.dragging = false;
    }  
  }
}

// Code based on "Scrollbar" by Casey Reas

HScrollbar[] hs = new HScrollbar[5];//
String[] labels =  {"separation", "alignment","cohesion","maxspeed","maxforce"};

int x = 5;
int y = 20;
int w = 500;
int h = 8;
int l = 2;
int spacing = 4;

public void setupScrollbars() {
  for (int i = 0; i < hs.length; i++) {
    hs[i] = new HScrollbar(x, y + i*(h+spacing), w, h, l);
  }

  hs[0].setPos(0.25f);
  hs[1].setPos(0.5f);
  hs[2].setPos(0.5f);
  hs[3].setPos(0.2f);
  hs[4].setPos(0.05f);
}

public void drawScrollbars() {
  swt = hs[0].getPos()*10.0f;     //sep.mult(25.0f);
  awt = hs[1].getPos()*2.0f;     //sep.mult(25.0f);
  cwt = hs[2].getPos()*2.0f;     //sep.mult(25.0f);
  maxspeed = hs[3].getPos()*10.0f;
  maxforce = hs[4].getPos()*0.5f;

  if (showScroll) {
    for (int i = 0; i < hs.length; i++) {
      hs[i].update();
      hs[i].draw();
      fill(255);
      textAlign(LEFT);
      text(labels[i],x+w+spacing,y+i*(h+spacing)+spacing);
      if (labels[i] == "separation") {
        text(swt,x+w+spacing + 150 ,y+i*(h+spacing)+spacing);
      }
      if (labels[i] == "cohesion") {
        text(cwt,x+w+spacing + 150 ,y+i*(h+spacing)+spacing);
      }
      if (labels[i] == "alignment") {
        text(awt,x+w+spacing + 150 ,y+i*(h+spacing)+spacing);
      }
      if (labels[i] == "maxspeed") {
        text(maxspeed,x+w+spacing + 150 ,y+i*(h+spacing)+spacing);
      }
      if (labels[i] == "maxforce") {
        text(maxforce,x+w+spacing + 150 ,y+i*(h+spacing)+spacing);
      }
      //text(hs[i].getPos(),x+w+spacing+75,y+i*(h+spacing)+spacing);
    }
  }
}


class HScrollbar
{
  int swidth, sheight;    // width and height of bar
  int xpos, ypos;         // x and y position of bar
  float spos, newspos;    // x position of slider
  int sposMin, sposMax;   // max and min values of slider
  int loose;              // how loose/heavy
  boolean over;           // is the mouse over the slider?
  boolean locked;
  float ratio;

  HScrollbar (int xp, int yp, int sw, int sh, int l) {
    swidth = sw;
    sheight = sh;
    int widthtoheight = sw - sh;
    ratio = (float)sw / (float)widthtoheight;
    xpos = xp;
    ypos = yp-sheight/2;
    spos = xpos;
    newspos = spos;
    sposMin = xpos;
    sposMax = xpos + swidth - sheight;
    loose = l;
  }

  public void update() {
    if(over()) {
      over = true;
    } 
    else {
      over = false;
    }
    if(mousePressed && over) {
      overScrollbar = true;
      locked = true;
    }
    if(!mousePressed) {
      locked = false;
      overScrollbar = false;
    }
    if(locked) {
      newspos = constrain(mouseX-sheight/2, sposMin, sposMax);
    }
    if(abs(newspos - spos) > 0) {
      spos = spos + (newspos-spos)/loose;
    }
  }

  public int constrain(int val, int minv, int maxv) {
    return min(max(val, minv), maxv);
  }

  public boolean over() {
    if(mouseX > xpos && mouseX < xpos+swidth &&
      mouseY > ypos && mouseY < ypos+sheight) {
      return true;
    } 
    else {
      return false;
    }
  }

  public void draw() {
    fill(255);
    rectMode(CORNER);
    rect(xpos, ypos, swidth, sheight);
    if(over || locked) {
      fill(153, 102, 0);
    } 
    else {
      fill(102, 102, 102);
    }
    rect(spos, ypos, sheight, sheight);
  }

  public void setPos(float s) {
    spos = xpos + s*(sposMax-sposMin);
    newspos = spos;
  }

  public float getPos() {
    // convert spos to be values between
    // 0 and the total width of the scrollbar
    return ((spos-xpos))/(sposMax-sposMin);// * ratio;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "inter_momenta" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
