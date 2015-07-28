package org.safs.selenium.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.SessionId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GridInfoExtractor{

	public static String[] getHostNameAndPort(String hostName, int port,
	                                           SessionId session) {
		String[] hostAndPort = new String[2];
		String errorMsg = "Failed to acquire remote webdriver node and port info. Root cause: ";

		try {
			HttpHost host = new HttpHost(hostName, port);
			HttpClient client = HttpClientBuilder.create().build();
			URL sessionURL = new URL("http://" + hostName + ":" + port + "/grid/api/testsession?session=" + session);
			BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", sessionURL.toExternalForm());
			HttpResponse response = client.execute(host, r);
			JSONObject object = extractObject(response);
			URL myURL = new URL(object.getString("proxyId"));
			if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
				hostAndPort[0] = myURL.getHost();
				hostAndPort[1] = Integer.toString(myURL.getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(errorMsg, e);
		}
		return hostAndPort;
	}

	private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer s = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		rd.close();
		JSONObject objToReturn = new JSONObject(s.toString());
		return objToReturn;
	}	
	
}
