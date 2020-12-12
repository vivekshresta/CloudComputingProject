package functions;

import functions.helper.PostBodyParser;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Client {

    public static Map<String, String> getData(String url, String functionality, JSONObject data) throws IOException {
        HttpURLConnection con = null;
        BufferedReader in = null;
        StringBuilder content = new StringBuilder();
        try {
            data.put("functionality", functionality);
            URL urlObj = new URL(url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = data.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                content.append(inputLine).append("\n");
        } finally {
            try {
                if(in != null)
                    in.close();
                if(con != null)
                    con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return PostBodyParser.populatePostBody(content.toString());
    }
}
