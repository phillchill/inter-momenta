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
