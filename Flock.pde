// Flocking
// Daniel Shiffman <http://www.shiffman.net>
// The Nature of Code, Spring 2011

class Flock {
  ArrayList<Boid> boids; // An ArrayList for all the boids

  Flock() {
    boids = new ArrayList<Boid>(); // Initialize the ArrayList
  }

  void run() {
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      if (b.isDead()) {
         it.remove();
      }
      else {
        b.run(boids);
      }
    }
  }

  void addBoid(Boid b) {
    boids.add(b);
  }
  
  void clear() {
    boids.clear();
  }
  
  void mousePressed(){
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      if (b.mouseOver()) {
        b.dragging = true;
      }
    }  
  }
  
  void mouseReleased(){
    Iterator<Boid> it = boids.iterator();
    while (it.hasNext()) {
      Boid b = it.next();
      b.dragging = false;
    }  
  }
}
