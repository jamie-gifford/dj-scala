package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.djs.disco.Types._
import au.com.thoughtpatterns.djs.disco.CollectionsUtil._
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.PrintWriter

object Artists {

  val tiArtists: Map[TINT, TiArtist] = {
    val tmp = readCsv("ti-tracks.csv",
      row => (parseTINT(row(7)), TiArtist(row(3), row(4))))
    tmp filter { x => x._1.isDefined } map { x => (x._1.get, x._2) }
  }

  // ----------------------------
  // Gordon support
  
  val gordonArtists: Map[GordonTrack, GordonArtist] =
    readCsv("gordon.csv",
      row => (GordonTrack(row(4), Integer.parseInt(row(5))), GordonArtist(row(1))))

  val gordonAlbumMap: Map[GordonAlbum, TiAlbumSide] =
    readCsv("gordon-ti-album-map.csv",
      row => (GordonAlbum(row(0)), TiAlbumSide(row(1), Integer.parseInt(row(2)))))

  /**
   * Want Map[GordonArtist, List[TiArtist]].
   * Algorithm: for each gordonTrack, find its GordonArtist,
   *   then find the equivalent TINT and its TiArtist.
   *   Finally, join these together
   */
  val gordonArtistMap: Map[GordonArtist, Set[TiArtist]] = {

    def findTint(a: GordonTrack): Option[TINT] = {
      gordonAlbumMap.get(GordonAlbum(a.albumName)) match {
        case Some(TiAlbumSide(tin, side)) => Some(TINT(tin, side, a.track))
        case _ => None
      }
    }

    val pairs = for {
      (track, artist) <- gordonArtists
      tint = findTint(track)
      if (tint.isDefined)
      a = tiArtists.get(tint.get)
      if (a.isDefined)
    } yield {
      (artist, a.get)
    }

    val mapOfLists = group(pairs.toList)

    mapOfLists map {
      x => (x._1, x._2.toSet)
    }

  }

  def generateGordonArtists(): Unit = {
    val utils = new CsvUtils();

    val list = for {
      (a, b) <- gordonArtistMap
      TiArtist(orchestra, vocalist) <- b
    } yield {
      List(a.name, orchestra, vocalist).toArray
    }
    utils.toCsv(list.toArray);
    new PrintWriter("gordon-ti-artist-map.csv").print(utils.getFormattedString())
  }
  
  // ---------------------------------
  // TDJ suport
  
  val tdjArtists: Map[TdjTrack, TdjArtist] =
    readCsv("tango-at.csv",
      row => (TdjTrack(row(8), Integer.parseInt(row(1))), TdjArtist(row(3))))

  val tdjAlbumMap: Map[TdjAlbum, TiAlbumSide] =
    readCsv("tdj-ti-album-map.csv",
      row => (TdjAlbum(row(0)), TiAlbumSide(row(1), Integer.parseInt(row(2)))))

  /**
   * Want Map[TdjArtist, List[TiArtist]].
   * Algorithm: for each tdjTrack, find its TdjArtist,
   *   then find the equivalent TINT and its TiArtist.
   *   Finally, join these together
   */
  val tdjArtistMap: Map[TdjArtist, Set[TiArtist]] = {

    def findTint(a: TdjTrack): Option[TINT] = {
      tdjAlbumMap.get(TdjAlbum(a.albumName)) match {
        case Some(TiAlbumSide(tin, side)) => Some(TINT(tin, side, a.track))
        case _ => None
      }
    }

    val pairs = for {
      (track, artist) <- tdjArtists
      tint = findTint(track)
      if (tint.isDefined)
      a = tiArtists.get(tint.get)
      if (a.isDefined)
    } yield {
      (artist, a.get)
    }

    val mapOfLists = group(pairs.toList)

    mapOfLists map {
      x => (x._1, x._2.toSet)
    }

  }

  def generateTDJArtists(): Unit = {
    val utils = new CsvUtils();

    val list = for {
      (a, b) <- tdjArtistMap
      TiArtist(orchestra, vocalist) <- b
    } yield {
      List(a.name, orchestra, vocalist).toArray
    }
    utils.toCsv(list.toArray);
    new PrintWriter("tdj-ti-artist-map.csv").print(utils.getFormattedString())
  }


  // ------------------------
  
  private val abbreviations = Map(
    "Adolfo Carabelli" -> "Carabelli",
    "Agustín Volpe" -> "Volpe",
    "Alberto Amor" -> "Amor",
    "Alberto Carol" -> "Carol",
    "Alberto Castillo" -> "Castillo",
    "Alberto Echagüe" -> "Echagüe",
    "Alberto Gómez" -> "Gómez",
    "Alberto Hilarion Acuña" -> "Hilarion Acuña",
    "Alberto Lago" -> "Lago",
    "Alberto Marino" -> "Marino",
    "Alberto Podestá" -> "Podestá",
    "Alberto Reynal" -> "Reynal",
    "Alberto Rivera" -> "Rivera",
    "Alfredo De Angelis" -> "De Angelis",
    "Amadeo Mandarino" -> "Mandarino",
    "Andrés Falgás" -> "Falgás",
    "Antonio Rodriguez Lesende" -> "Rodriguez Lesende",
    "Aníbal Troilo" -> "Troilo",
    "Armando Laborde" -> "Laborde",
    "Armando Moreno" -> "Moreno",
    "Carlos Acuña" -> "Acuña",
    "Carlos Bermúdez" -> "Bermúdez",
    "Carlos Dante" -> "Dante",
    "Carlos Di Sarli" -> "Di Sarli",
    "Carlos Galarce" -> "Galarce",
    "Carlos Galán" -> "Galán",
    "Carlos Heredia" -> "Heredia",
    "Carlos Lafuente" -> "Lafuente",
    "Carlos Roldán" -> "Roldán",
    "Carlos Saavedra" -> "Saavedra",
    "Edgardo Donato" -> "Donato",
    "Edmundo Rivero" -> "Rivero",
    "Eduardo Adrián" -> "Adrián",
    "Enrique Campos" -> "Campos",
    "Enrique Carbel" -> "Carbel",
    "Enrique Rodríguez" -> "Rodríguez",
    "Ernesto Famá" -> "Famá",
    "Fernando Díaz" -> "Díaz",
    "Fernando Reyes" -> "Reyes",
    "Floreal Ruíz" -> "Ruíz",
    "Francisco Amor" -> "Amor",
    "Francisco Canaro" -> "Canaro",
    "Francisco Fiorentino" -> "Fiorentino",
    "Francisco Lomuto" -> "Lomuto",
    "Félix Gutiérrez" -> "Gutiérrez",
    "Horacio Lagos" -> "Lagos",
    "Horacio Quintana" -> "Quintana",
    "Hugo Duval" -> "Duval",
    "Hugo del Carril" -> "del Carril",
    "Héctor Farrel" -> "Farrel",
    "Héctor Juncal" -> "Juncal",
    "Héctor Mauré" -> "Mauré",
    "Jorge Durán" -> "Durán",
    "Jorge Linares" -> "Linares",
    "Jorge Omar" -> "Omar",
    "Jorge Ortiz" -> "Ortiz",
    "Juan Carlos Casas" -> "Casas",
    "Juan Carlos Delson" -> "Delson",
    "Juan Carlos Miranda" -> "Miranda",
    "Juan D'Arienzo" -> "D'Arienzo",
    "Julio De Caro" -> "De Caro",
    "Julio Martel" -> "Martel",
    "Julián Centeya" -> "Centeya",
    "Lita Morales" -> "Morales",
    "Lucio Demare" -> "Demare",
    "Luis Díaz" -> "Díaz",
    "Luis Tolosa" -> "Tolosa",
    "Mariano Balcarce" -> "Balcarce",
    "Mario Pomar" -> "Pomar",
    "Martín Podestá" -> "Podestá",
    "Mercedes Simone" -> "Simone",
    "Miguel Caló" -> "Caló",
    "Miguel Montero" -> "Montero",
    "Miguel Villasboas" -> "Villasboas",
    "Néstor Rodi" -> "Rodi",
    "Orlando Medina" -> "Medina",
    "Orq. típica Víctor" -> "OTV",
    "Orquesta Típica Víctor" -> "OTV",
    "Ortega del Cerro" -> "del Cerro",
    "Oscar Larroca" -> "Larroca",
    "Osvaldo Fresedo" -> "Fresedo",
    "Osvaldo Pugliese" -> "Pugliese",
    "Osvaldo Ribó" -> "Ribó",
    "Pedro Laurenz" -> "Laurenz",
    "Raúl Aldao" -> "Aldao",
    "Raúl Berón" -> "Berón",
    "Raúl Iriarte" -> "Iriarte",
    "Ricardo Malerba" -> "Malerba",
    "Ricardo Ruiz" -> "Ruiz",
    "Ricardo Tanturi" -> "Tanturi",
    "Roberto Arrieta" -> "Arrieta",
    "Roberto Chanel" -> "Chanel",
    "Roberto Díaz" -> "Díaz",
    "Roberto Firpo" -> "Firpo",
    "Roberto Flores" -> "Flores",
    "Roberto Lemos" -> "Lemos",
    "Roberto Maida" -> "Maida",
    "Roberto Ray" -> "Ray",
    "Roberto Rufino" -> "Rufino",
    "Roberto Videla" -> "Videla",
    "Roberto Zerrillo" -> "Zerrillo",
    "Rodolfo Biagi" -> "Biagi",
    "Romeo Gavioli" -> "Gavioli",
    "Teofilo Ibañez" -> "Ibañez",
    "Tino García" -> "García",
    "Vicente Crisera" -> "Crisera",
    "Walter Cabral" -> "Cabral",
    "Ángel D'Agostino" -> "D'Agostino",
    "Ángel Ramos" -> "Ramos",
    "Ángel Vargas" -> "Vargas")
    
  def abbrev(artist: String) = abbreviations.getOrElse(artist, artist)


  def main(args: Array[String]): Unit = {
    generateGordonArtists();
    generateTDJArtists();
  }
  

}
