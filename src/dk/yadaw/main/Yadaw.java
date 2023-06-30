package dk.yadaw.main;

public class Yadaw {
	private static YadawController yaController;
	
	public static void main( String args[] ) {
		System.out.println( "Work dir: " + System.getProperty("user.dir"));
		yaController = new YadawController();
	}
}
