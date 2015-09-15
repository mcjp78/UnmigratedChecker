package pw.cinque.checker;

import java.io.File;

public class Checker {

	public Checker(String[] args) {
		File usernames = new File(System.getProperty("user.dir") + File.separator + "usernames.txt");
		
		if (!usernames.exists()) {
			System.out.println("Cannot find file 'usernames.txt'. Please put it in the same directory as Username Checker!");
			return;
		}
		
		new Thread(new CheckerRunnable(usernames)).start();
	}

}
