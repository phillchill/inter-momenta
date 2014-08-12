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
  ArrayList<String> loadLines(String filename) {
    ArrayList<String> lines = new ArrayList<String>();
    String[] rawLines = loadStrings(filename);
    for(String str : rawLines) {
      if(str != null && !str.isEmpty()){
        lines.add(str);
      }
    }
    return lines;
  }
  
  String getLine(ArrayList<String> lines) {
    String line = "";
    while(line.isEmpty()){
      line = lines.get(r.nextInt(lines.size()));  
    }
    return line;
  }
  
  // pick randomm filtered token from specific line
  String getToken(String line){  
    String[] tokensArr = splitTokens(line, " &,+.'`");
    ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(tokensArr));
    
    // filter tokens
    tokens = filterTokens(tokens);
    String token = tokens.get(r.nextInt(tokens.size()));
    return token;
  }
  
  ArrayList<String> filterTokens (ArrayList<String> tokens) {
    Iterator<String> it = tokens.iterator();
    while (it.hasNext()) {
      String token = it.next();
      if (filterTokens.contains(token) || token.length() < 2 ){
        it.remove();
      }
    }
    return tokens;
  }
  
  // pick random token from global lines
  String newToken(){
    String line =  getLine(lines);
    String token = getToken(line);
    return token;
  }
}
