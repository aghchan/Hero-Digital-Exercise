package com.company;

import org.json.JSONArray;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Enter GitHub Username of Repository's to List: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine().trim();

        System.out.println("Enter Maximum Number of Results to Display: ");
        String maxNumOfResults = scanner.nextLine().trim();

        GitHubService.performGitHubAPICallAndWriteToFile(username, maxNumOfResults);

    }
}
