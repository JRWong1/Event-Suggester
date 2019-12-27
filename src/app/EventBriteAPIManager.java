package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class EventBriteAPIManager {
	
	private static HttpURLConnection con;
	
	public static void main(String[] args) {
		
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		
		URL url;
		try {
			url = new URL("https://jsonplaceholder.typicode.com/albums");
			con = (HttpURLConnection) url.openConnection();
			
			//request setup
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setRequestProperty("Content-Type", "application/json");
			
			int status = con.getResponseCode();
			//System.out.println(status);
			
			if(status > 299) {
				reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			while((line = reader.readLine()) != null) {
				responseContent.append(line);
			}
			reader.close();
			
			parse(responseContent.toString());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			con.disconnect();
		}
		
	}
	
	public static String parse(String responseBody) {
		JSONArray albums = new JSONArray(responseBody);
		for(int i = 0; i < albums.length(); i++) {
			JSONObject album = albums.getJSONObject(i);
			//where key is id
			int id = album.getInt("id");
			int userId = album.getInt("userId");
			String title = album.getString("title");
			System.out.println(id + "  " + title + "  " + userId);
		}
		return null;
	}
	
}

