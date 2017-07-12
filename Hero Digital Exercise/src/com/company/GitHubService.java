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

    private static Object getJSONResponseFromAPI(URL url) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String responseLine;
        while (reader != null && ((responseLine = reader.readLine()) != null)) {
            sb.append(responseLine);
        }
        return new JSONTokener(sb.toString()).nextValue();
    }

    public static void performGitHubAPICall(String username, String maxNumOfResults) {
        try {
            Object json = getJSONResponseFromAPI(new URL(HOSTNAME.concat(username).concat(CONTEXT)));
            if (json instanceof JSONArray) {
                int jsonSize = ((JSONArray) json).length();
                int maxNum = Integer.parseInt(maxNumOfResults);
                int maxRows = jsonSize > maxNum ? maxNum : jsonSize;
                PrintWriter writer = new PrintWriter(OUTPUT_FILE, CHARSET);
                String[] fileValues = new String[maxRows * ColumnNames.values().length];
                HashMap<String, Integer> valueLengths = new HashMap<>();
                storeJSONAndCheckSizeForAllColumns(maxRows, fileValues, valueLengths, (JSONArray) json);
                writeFieldsToFile(writer, valueLengths, fileValues, maxRows);
                writer.close();
            } else {
                LOGGER.log(Level.WARNING, "Did not perform GitHub User Repo API Call");
            }
        } catch(FileNotFoundException fnfEx) {
            LOGGER.log(Level.SEVERE, "Exception occured, issue performing API call", fnfEx);
        } catch(NumberFormatException nfEx) {
            LOGGER.log(Level.SEVERE, "Exception occured, number of results to display is not an integer", nfEx);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occured", ex);
        }
    }

    private static String printFormatting(HashMap<String, Integer> formattingSpaces, String columnName) {
        return "%-".concat(Integer.toString(formattingSpaces.get(columnName) + PADDING_SPACES)).concat("s");
    }

    private static void storeJSONAndCheckSizeForAllColumns(int maxRows, String[] fileValues, HashMap<String, Integer> valueLengths, JSONArray jsonArray) {
        for (int r = 0; r < maxRows; r++) {
            JSONObject jsonObject = jsonArray.getJSONObject(r);
            int totalNumOfColumns = ColumnNames.values().length;
            for (int c = 0; c < totalNumOfColumns; c++) {
                ColumnNames columnName = ColumnNames.values()[c];
                String nestedEntry = columnName.getNestedEntry();
                String columnNameValue = columnName.getName();
                int arrayIndex = r * totalNumOfColumns + c;
                fileValues[arrayIndex] = nestedEntry.isEmpty() ? (String) jsonObject.get(columnNameValue)
                        : (String) jsonObject.getJSONObject(nestedEntry).get(columnNameValue);
                if (valueLengths.get(columnNameValue) == null || fileValues[arrayIndex].length() > valueLengths.get(columnNameValue)) {
                    valueLengths.put(columnNameValue, fileValues[arrayIndex].length());
                }
            }
        }
    }

    private static void writeFieldsToFile(PrintWriter writer, HashMap<String, Integer> valueLengths, String[] fileValues, int maxRows) {
        int totalNumOfColumns = ColumnNames.values().length;
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < totalNumOfColumns; j++) {
                writer.printf(printFormatting(valueLengths, ColumnNames.values()[j].getName()), fileValues[totalNumOfColumns * i + j]);
            }
            writer.println();
        }
    }
}
