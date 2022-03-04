package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SQLToJSONConverter {

	private static List<String> getSQLfromFile(String filename) throws IOException {
		String content = new String(Files.readAllBytes(new File("files/sql/" + filename).toPath()));
		String[] sqlStatements = content.split("--\n");
		return Arrays.asList(sqlStatements);
	}
	
	private static List<List<String>> statementsSplit(String filename) throws IOException {
		System.out.println("Get sql statements from file ....");
		List data = new ArrayList<>(),
					tables= new ArrayList<>(),
					foreign_key = new ArrayList<>(),
					primary_key = new ArrayList<>();
		
		for(String statement : getSQLfromFile(filename)) {
			if(statement.contains("INSERT")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				data.add(statement);
				
				statement = statement.split(" ")[2].replaceAll("`", "");
				tables.add(statement);
				continue;
			}
			
			if(statement.contains("FOREIGN KEY")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				Matcher m = Pattern.compile("`.*?`").matcher(statement);
				List<String> r = new ArrayList<>();
				while (m.find()) 
					r.add(m.group().replaceAll("`", ""));
				foreign_key.add(r);
				continue;
			}
			
			if(statement.contains("PRIMARY KEY")) {
				statement = statement.replaceAll("\n", "");
				statement = statement.replaceAll("----[^--]+--","");
				primary_key.add(statement);
				continue;
			}
		}
		String temp = String.join("  ", data);
		data.clear();
		List<String> dat = Arrays.asList( temp.split("\\);") );
		dat = dat.subList(0, dat.size());

		for(String r : dat) {
			r = r.replaceAll("^-- ------------------------------------------------------  ", "");
			data.add( String.join(" ", r, ");") );
		}
		
		List primaryKeyData = new ArrayList<>();
		for(Object obj : primary_key) {
			String key = (String) obj;
			List<String> k = new ArrayList<>();
			Matcher m = Pattern.compile("\\`.*?\\`").matcher(key);
			while (m.find()) 
				k.add(m.group().replaceAll("`", ""));
			primaryKeyData.add(k.subList(0, 2));
		}
		
		List foreign = new ArrayList<>();
		for(Object obj : foreign_key) {
			List<String> k = (List<String>) obj;
			if( tables.contains(k.get(0)) ) {
				foreign.add(k);
			}
		}
		
		List info = new ArrayList<>();
		info.add(tables);
		info.add(data);
		info.add(foreign);
		info.add(primaryKeyData);
		
		return info;
	}

}
