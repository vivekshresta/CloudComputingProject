package functions.helper;

import com.google.cloud.functions.HttpRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PostBodyParser {

    public static Map<String, String> getPostBody(HttpRequest request) {
        StringBuilder content = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                content.append(inputLine).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return populatePostBody(content.toString());
    }

    private static Map<String, String> populatePostBody(String body) {
        Map<String, String> parameterMap = new HashMap<>();
        try {
            JSONObject userDetails = (JSONObject) new JSONParser().parse(body);
            for(Object key: userDetails.keySet())
                parameterMap.put((String)key, (String)userDetails.get(key));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return parameterMap;
    }
}
