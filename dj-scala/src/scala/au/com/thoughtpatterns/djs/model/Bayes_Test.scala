package au.com.thoughtpatterns.djs.model

object Bayes_Test {

    def main(args: Array[String]) = {

      val data = List(
        List("A","fine","day"),
        List("A","lonely","day"),
        List("The","first","morning")
      )
      
      val model = new Bayes[String](data);

      println(model.likelihood(List("A","happy","day"), List("A","first","day")));
      
    }
  
}