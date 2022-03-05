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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SQLToJSONConverter {

	private static List<String> getSQLfromFile(String filename) throws IOException {
		String content = new String(Files.readAllBytes(new File(filename).toPath()));
		String[] sqlStatements = content.split("--\n");
		return Arrays.asList(sqlStatements);
	}
	
	private static Map<String, List> statementsSplit(String filename) throws IOException {
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
		
		Map<String, List> info = new HashMap<>();
		info.put("tables", tables);
		info.put("data", data);
		info.put("foreign", foreign);
		info.put("primary", primaryKeyData);
		
		return info;
	}
	
	private static JSONObject getJSONFromSQLStatements(List<String> SQLStatements) {
		JSONObject json = new JSONObject();
		
		SQLStatements.forEach(sql -> {
			String[] metadata = sql.split("VALUES");
			String table = metadata[0].split("`")[1];
			String[] columns = metadata[0].substring(metadata[0].indexOf("(")+1, metadata[0].indexOf(")")).replaceAll("`", "").split(",");
			String[] rows = metadata[1].split("\\),\\(");
			
			JSONArray tableData = new JSONArray();
			for(int i=0; i<rows.length; i++) {
				JSONObject row = new JSONObject();
				String[] values = rows[i].replaceAll("[\\(\\)';]", "").split(",");
				for(int j=0; j<columns.length; j++)
					row.put(columns[j].trim(), values[j].trim() );
				tableData.add(row);
			}
			json.put(table, tableData);
		});
		return json;
	}
	
	public static void generateJSONFilesFromSQLFile(String filename) throws IOException {
			Map<String, List> info = statementsSplit(filename);
			List data = info.get("data");
			List foreign = info.get("foreign");			
			JSONObject json = getJSONFromSQLStatements(data);
			
			try {
				JSONObject clonedJSON = (JSONObject)new JSONParser().parse(json.toJSONString());
				for(List<String> k : (List<List<String>>)foreign) {
					for(Object obj : (JSONArray)clonedJSON.get(k.get(0))) {
						JSONObject left = (JSONObject) obj;
						List arr = new ArrayList<>();
						((JSONArray)json.get(k.get(3))).forEach(item -> {
							JSONObject x = (JSONObject) item;
							if( ((String)x.get(k.get(4))).equals( ((String)left.get(k.get(2))) ) )
								arr.add(x);
							left.put(k.get(3), arr);
						});
					}
					
					for(Object obj : (JSONArray)clonedJSON.get(k.get(3))) {
						JSONObject left = (JSONObject) obj;
						List arr = new ArrayList<>();
						((JSONArray)json.get(k.get(0))).forEach(item -> {
							JSONObject x = (JSONObject) item;
							if( ((String)x.get(k.get(2))).equals( ((String)left.get(k.get(4))) ) ) {
								arr.add(x);
							}
							left.put(k.get(0), arr);
						});
					}
				}
				
				FileSaver.saveDataAsJSON("json_no_ref", json);
				FileSaver.saveDataAsJSON("json_with_ref", clonedJSON);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
