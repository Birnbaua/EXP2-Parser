package dbPedia;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Felix Winterleitner
 *
 */
public class DBPediaAirportLinker {
    private final String sparqlEndpoint = "http://dbpedia.org/sparql";
    private final String query1 = "select distinct ?airport ?icaocode\n" +
            "where {?airport dbo:icaoLocationIdentifier ?icaocode.\n" +
            "FILTER regex(?icaocode, \"^....$\")}\n" +
            "order by asc(?icaocode)";
    private final String query2 = "select distinct ?airport ?icaocode\n" +
            "where {?airport dbo:icaoLocationIdentifier ?icaocode.\n" +
            "FILTER regex(?icaocode, \"^....$\")}\n" +
            "order by desc(?icaocode)";

    private final Map<String, String> airports;

    public DBPediaAirportLinker(){
        airports = mergeResults(receiveJSON(query1), receiveJSON(query2));
        /*for (String key: airports.keySet()) {
            System.out.println(key + ": " + airports.get(key));
        }*/
    }

    public String getAirportURI(String icao) {
        return airports.get(icao) != null ? airports.get(icao.toUpperCase()) : "";
    }

    private Map<String, String> receiveJSON(String query) {
        String response = "";
        try {
            URL url = new URL(sparqlEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            Map<String, String> parameters = new HashMap<>();
            parameters.put("query", query);
            parameters.put("format", "application/json");

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
            response = FullResponseBuilder.getFullResponse(con);
        }
        catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        Map<String, String> airports = new HashMap<>();

        JSONObject obj = new JSONObject(response);
        JSONArray aps = obj.getJSONObject("results").getJSONArray("bindings");

        for (int i = 0; i < aps.length(); i++) {
            JSONObject ap = aps.getJSONObject(i);
            if (ap.getJSONObject("icaocode").getString("value").length() > 0) {
                airports.put(ap.getJSONObject("icaocode").getString("value").toUpperCase(), ap.getJSONObject("airport").getString("value"));
            }
        }
        return airports;
    }

    private Map<String, String> mergeResults(Map<String, String> a, Map<String, String> b) {
        a.putAll(b);
        return a;
    }
}

class ParameterStringBuilder {
    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}

class FullResponseBuilder {
    public static String getFullResponse(HttpURLConnection con) throws IOException {
        StringBuilder fullResponseBuilder = new StringBuilder();

        fullResponseBuilder.append(con.getResponseCode())
                .append(" ")
                .append(con.getResponseMessage())
                .append("\n");

        con.getHeaderFields()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .forEach(entry -> {

                    fullResponseBuilder.append(entry.getKey())
                            .append(": ");

                    List<String> headerValues = entry.getValue();
                    Iterator<String> it = headerValues.iterator();
                    if (it.hasNext()) {
                        fullResponseBuilder.append(it.next());

                        while (it.hasNext()) {
                            fullResponseBuilder.append(", ")
                                    .append(it.next());
                        }
                    }

                    fullResponseBuilder.append("\n");
                });

        Reader streamReader = null;

        if (con.getResponseCode() > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        fullResponseBuilder.append("Response: ")
                .append(content);
        return content.toString();
        //return fullResponseBuilder.toString();
    }
}
