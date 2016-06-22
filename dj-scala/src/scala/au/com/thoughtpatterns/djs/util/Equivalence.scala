package au.com.thoughtpatterns.djs.util

class Equivalence[T] protected (val identified: Set[T], val base: Equivalence[T]) {

  class Coset(val members: Set[T]) {
    
    def rep = members.head
    
  }

  type C = Equivalence[T]#Coset
  
  val cosets: Map[T, C] = {
    
    val mergedCosets = if (base != null) 
        identified flatMap { t => base.cosets.get(t) }
      else 
        Set()
      
    val mergedCosetMembers = ( mergedCosets flatMap { c => c.members } ) ++ identified
    val mergedCoset = new Coset(mergedCosetMembers)

    val baseKeys = if (base != null)
        base.cosets.keys
      else
        Set()  
    
    val z = (for (t <- mergedCosetMembers) yield (t -> mergedCoset)).toMap   
        
    if (base != null) base.cosets ++ z else z;
  }

  def equiv(a: T, b: T): Boolean = coset(a) == coset(b)

  def coset(t: T): C = cosets.getOrElse(t, new Coset(Set(t)))
  
  def ++ (i: Set[T]) = 
    if (i.size <= 1)
      this
    else
      new Equivalence(i, this)
}

object Equivalence {
  
  def apply[T](identify: Set[T]) = 
    if (identify.size > 1) 
      new Equivalence(identify, null) 
    else 
      new Equivalence(Set[T](), null)
  
}
