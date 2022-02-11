package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONArray;

public class FileSaver {

	public static final String FOLDER_PATH = "files/";

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
