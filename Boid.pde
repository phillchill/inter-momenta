// Inspired by
// Daniel Shiffman <http://www.shiffman.net>
// The Nature of Code, 2011

// Boid class

float swt;
float cwt;
float awt;
float maxspeed = 1;
float maxforce = 0.025;



class Boid {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float gazeRad;
  float r;
  
  float hoverRad;
  boolean hovered = false;
  boolean dragging = false; 
  
  int lifespan;
  int leftToLive;
  int birth;
  
  boolean wrapAround;
  ArrayList<Boid> nbs;
  
  String word;
  
  float radialEasing = 0.03;
  float reactionRadial = 0;
  float reactionRadialMax = 100;
  
  float surfaceEasing = 0.3;
  float textSize = 0;
  float textSizeMax = 20;

  // Constructor initialize all values
  Boid(float x, float y, ArrayList<Boid> peers, String token) {
    location = new PVector(x, y);
    maxspeed = 1.5;
    maxforce = 0.25;
    acceleration = new PVector(0, 0);
    velocity = new PVector(random(-0.5*maxspeed,0.5*maxspeed), random(-0.5*maxspeed,0.5*maxspeed));
    r = 3;
    gazeRad = 100;
    wrapAround = false;
    
    lifespan = 60000;
    leftToLive = lifespan;
    birth = millis();
    
    word = token;
    
    // connect 2 neighbours
    connect2Neighbours(peers);
  }
  
  void connect2Neighbours(ArrayList<Boid> peers){
    nbs = new ArrayList<Boid>();
    // For every peer boid, check if it's in the gaze radius
    for (Boid other : peers) {
      float d = PVector.dist(location, other.location);
      if(d > 0 && d < gazeRad){
        nbs.add(other);
      }
    }
    synth.out.playNote(0, .5, 50+nbs.size()*100);
  }
  
  void applyForce(PVector force) {
    // We could add mass here if we want A = F / M
    acceleration.add(force);
  }
  
  // Separation
  // Method checks for nearby vehicles and calculates steer away force
  PVector separate (ArrayList<Boid> vehicles) {
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
  PVector cohesion (ArrayList<Boid> boids) {
    float neighbordist = 90.0;
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
  PVector align (ArrayList<Boid> boids) {
    float neighbordist = 50.0;
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
  PVector seek(PVector target) {
    PVector desired = PVector.sub(target,location);  // A vector pointing from the location to the target

    // Normalize desired and scale to maximum speed
    desired.normalize();
    desired.mult(maxspeed);
    // Steering = Desired minus Velocity
    PVector steer = PVector.sub(desired,velocity);
    steer.limit(maxforce);  // Limit to maximum steering force

    return steer;
  }

  void run(ArrayList<Boid> boids) {
    flock(boids);
    update();
    borders();
    display();
  }
  
  void flock (ArrayList<Boid> boids) {
    PVector sep = separate(boids);   // Separation
    PVector ali = align(boids);      // Alignment
    PVector coh = cohesion(boids);   // Cohesion
    // Arbitrarily weight these forces
    
    sep.mult(swt);
    ali.mult(awt);
    coh.mult(cwt);
    // Add the force vectors to acceleration
    applyForce(sep);
    applyForce(ali);
    applyForce(coh);
  }

  // Method to update location
  void update() {
    if(!dragging){
      // Update velocity
      velocity.add(acceleration);
      // Limit speed
      velocity.limit(maxspeed);
      location.add(velocity);
      // Reset accelertion to 0 each cycle
      acceleration.mult(0);  
    }
    else {
      location.x = mouseX;
      location.y = mouseY;
    }
    
    
    leftToLive = lifespan - (millis() - birth);
  }

  void display() {
    // appear
    float dRadial = reactionRadialMax - reactionRadial;
    if(dRadial != 0){
      noStroke();
      reactionRadial += abs(dRadial) * radialEasing;
      fill(255,255,255,150-reactionRadial*(255/reactionRadialMax));
      ellipse(location.x, location.y, reactionRadial, reactionRadial);
    }
    
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
    
    // draw connections to neighboars
    gaze();
    
    // draw text
    // textSize(20);
    
    float lifeline = map(leftToLive, 0, lifespan, 25, 255);
    stroke(255);
//    line(location.x, location.y, location.x+lifeline, location.y);
//    fill(255, 255, 255, lifeline);
    fill(255, 255, 255);
    text(word, location.x, location.y);
  }
  
  void gaze() {
    // For every neighbour, draw line
    float lifeline = map(leftToLive, 0, lifespan, 20, 255);
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
    if(mouseDist < hoverRad) {
      hovered = true;
    }
    else {
      hovered = false;
    }
    
  }

  
  void borders() {
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
  
  boolean isDead(){
    return !(leftToLive > 0);
  }
  
  boolean mouseOver(){
    float mouseDist = PVector.dist(location, new PVector(mouseX,mouseY));
    return mouseDist < 30;
  }

}
