package au.com.thoughtpatterns.djs.util

object Equivalence_Test {

  def main(args: Array[String]) = {
    
    val disarli = Set("Di Sarli", "di Sarli", "disarli")
    
    val darienzo = Set("D'Arienzo", "Darienzo")
    
    val e = Equivalence(disarli)
    val f = e ++ darienzo
    
    println("1. " + f.equiv("A", "B"))
    println("2. " + f.equiv("Di Sarli", "D'Arienzo"))
    println("3. " + f.equiv("Di Sarli", "disarli"))
    println("4. " + e.equiv("D'Arienzo", "Darienzo"))
    println("5. " + f.equiv("D'Arienzo", "Darienzo"))
    
  }
  
}