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

	public static String saveDataAsJSON(String parentFolder, Map<String, JSONArray> data) throws IOException {
		String filespath = FOLDER_PATH + parentFolder + "/";
		Files.createDirectories(Paths.get(filespath));
		data.forEach((table, rows) -> {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filespath + table + ".json"))) {
				writer.write(rows.toJSONString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return filespath;
	}
}
