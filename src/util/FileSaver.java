package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.json.simple.JSONArray;

public class FileSaver {

	public static final String FOLDER_PATH = "files/";
	
	static {
		try {
			Files.createDirectory(Paths.get(FOLDER_PATH));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDataAsJSON(Map<String,JSONArray> data) {
		data.forEach((table, rows)->{
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(FOLDER_PATH + table+".json"))) {
				writer.write(rows.toJSONString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
