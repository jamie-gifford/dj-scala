package au.com.thoughtpatterns.djs.clementine;

public class PlayerInterfaceFactory {

	private static PlayerInterface player = new Clementine();
	
	public static PlayerInterface getPlayer() {
		return player;
	}
	
	public static void useAudacious() {
		player = new Audacious();
	}

	public static void useClementine() {
		player = new Clementine();
	}

}
