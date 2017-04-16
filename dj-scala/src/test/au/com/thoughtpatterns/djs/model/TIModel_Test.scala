package au.com.thoughtpatterns.djs.model

object TIModel_Test {

  def main(args: Array[String]) {
    val model = TIModel();
//    val songs = model.identify("A la gran Muñeca", "di Sarli, Carlos");
//    for (s <- songs.take(10)) println(s)
    
    val words = model.tokenise("Juan D'Arienzo y su Orquesta Típica");
    
    for (w <- words) {
      val r = model.artistModel.ratio(w)
      println(w + ": " + r)
    }
    
  }
  
}