package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLToJSONConverter {

	private List<String> getSQLfromFile(String filename) throws IOException {
		String content = new String(Files.readAllBytes(new File("files/sql/" + filename).toPath()));
		String[] sqlStatements = content.split("--\n");
		return Arrays.asList(sqlStatements);
	}

	private Map<String, List<String>> splitStatements(String filename) throws IOException {
		System.out.println("Get sql statements from file ....");
		Map<String, List<String>> infos = new HashMap<>(4);
		for (String key : new String[] { "data", "tables", "foreign_key", "primary_key" })
			infos.put(key, new ArrayList<>());
		
		for(String statement : getSQLfromFile(filename)) {
			if(statement.contains("INSERT")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				infos.get("data").add(statement);
				
				statement = statement.split(" ")[2].replaceAll("`", "");
				infos.get("tables").add(statement);
				continue;
			}
			
			if(statement.contains("FOREIGN KEY")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				Matcher m = Pattern.compile("your regular expression here").matcher(statement);
				while (m.find()) 
					infos.get("foreign_key").add(m.group().replaceAll("`", ""));
				continue;
			}
			
			if(statement.contains("PRIMARY KEY")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				infos.get("primary_key").add(statement);
				continue;
			}
		}
		return infos;
	}
}
