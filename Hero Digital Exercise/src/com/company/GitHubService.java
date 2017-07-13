package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Alan on 7/11/2017.
 */
public class GitHubService {
    private static final String HOSTNAME = "https://api.github.com/users/";
    private static final String CONTEXT = "/repos";
    private static final String OUTPUT_FILE = "List-of-GitHub-Repositories.txt";
    private static final String CHARSET = "UTF-8";
    private static final int PADDING_SPACES = 10;
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());


    public static void performGitHubAPICallAndWriteToFile(String username, String maxNumOfResults) {
        try {
            int maxRows = Integer.parseInt(maxNumOfResults);
            Object json = getJSONResponseFromAPI(new URL(HOSTNAME.concat(username).concat(CONTEXT)));
            if (json != null && json instanceof JSONArray) {
                writeFieldsToFile((JSONArray) json, maxRows);
            } else {
                LOGGER.log(Level.WARNING, "Issue with GitHub API response, did not write to file");
            }
        } catch(NumberFormatException nfEx) {
            LOGGER.log(Level.SEVERE, "Exception occurred, number of results to display is not an integer", nfEx);
        } catch(FileNotFoundException fnfEx) {
            LOGGER.log(Level.SEVERE, "Exception occurred, issue performing GitHub API call", fnfEx);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred", ex);
        }
    }

    private static void writeFieldsToFile(JSONArray jsonArray, int maxNumberOfRows) {
        try {
            int jsonSize = jsonArray.length();
            int maxRows = jsonSize > maxNumberOfRows  ? maxNumberOfRows : jsonSize;
            PrintWriter writer = new PrintWriter(OUTPUT_FILE, CHARSET);
            HashMap<String, Integer> columnWidths = getColumnWidthHash(maxRows, jsonArray);

            for (int r = 0; r < maxRows; r++) {
                JSONObject jsonObject = jsonArray.getJSONObject(r);
                for (int c = 0; c < ColumnNames.values().length; c++) {
                    String columnNameValue = ColumnNames.values()[c].getColumnNameValue(jsonObject);
                    writer.printf(printFormatting(columnWidths, ColumnNames.values()[c].getName()), columnNameValue);
                }
                writer.println();
            }
            writer.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occured", ex);
        }
    }

    private static Object getJSONResponseFromAPI(URL url) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String responseLine;

        while (reader != null && ((responseLine = reader.readLine()) != null)) {
            sb.append(responseLine);
        }
        reader.close();
        return new JSONTokener(sb.toString()).nextValue();
    }

    private static String printFormatting(HashMap<String, Integer> formattingSpaces, String columnName) {
        return "%-".concat(Integer.toString(formattingSpaces.get(columnName) + PADDING_SPACES)).concat("s");
    }

    private static HashMap<String, Integer> getColumnWidthHash(int maxRows, JSONArray jsonArray) {
        HashMap<String, Integer> columnWidths = new HashMap<>();
        int totalNumOfColumns = ColumnNames.values().length;
        for (int r = 0; r < maxRows; r++) {
            JSONObject jsonObject = jsonArray.getJSONObject(r);
            for (int c = 0; c < totalNumOfColumns; c++) {
                String columnName = ColumnNames.values()[c].getName();
                String columnNameValue = ColumnNames.values()[c].getColumnNameValue(jsonObject);
                int columnNameValueLength = columnNameValue.length();

                if (columnWidths.get(columnName) == null || columnWidths.get(columnName) < columnNameValueLength) {
                    columnWidths.put(columnName, columnNameValueLength);
                }
            }
        }
        return columnWidths;
    }


}
