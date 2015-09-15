package pw.cinque.checker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CheckerRunnable implements Runnable {

	private File usernames;

	public CheckerRunnable(File usernames) {
		this.usernames = usernames;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new FileReader(usernames)); BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
			ArrayList<String> names = new ArrayList<String>();

			// Read the username list.
			String readLine;
			while ((readLine = reader.readLine()) != null) {
				names.add(readLine.toLowerCase());
			}

			// Cancel if the list is empty.
			if (names.size() == 0) {
				System.out.println("File 'usernames.txt' is empty!");
				System.exit(-1);
				return;
			}

			URL url = new URL("https://api.mojang.com/profiles/minecraft");
			JSONParser parser = new JSONParser();

			// Make sure every request contains 100 or less usernames.
			for (int i = 0; i < Math.ceil(names.size() / 100D); i++) {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				OutputStream stream = connection.getOutputStream();

				int amount = Math.min(i * 100 + 100, names.size());
				String body = JSONArray.toJSONString(names.subList(i * 100, amount));

				stream.write(body.getBytes());
				stream.close();

				JSONArray result = (JSONArray) parser.parse(new InputStreamReader(connection.getInputStream()));

				for (Object profile : result) {
					JSONObject user = (JSONObject) profile;

					// If the account is unmigrated, save it to the output file.
					if (user.containsKey("legacy")) {
						writer.write((String) user.get("name"));
						writer.newLine();
					}
				}

				System.out.println("Processed " + amount + " usernames... (" + (Math.round((double) amount / names.size() * 1000D) / 10D) + "%)");
			}
			
			System.out.println("Processing finished!");
			System.out.println("Available usernames saved to 'output.txt'.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
