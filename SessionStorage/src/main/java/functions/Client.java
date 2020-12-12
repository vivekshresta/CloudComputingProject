package functions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client {

    public static String getData(String url) throws IOException {
        HttpURLConnection con = null;
        BufferedReader in = null;
        StringBuilder content = new StringBuilder();
        try {
            URL urlObj = new URL(url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");

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

        return content.toString();
    }
}
