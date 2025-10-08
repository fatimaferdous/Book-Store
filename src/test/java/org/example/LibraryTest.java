package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    @Test
    @DisplayName("Check library catalogue size is 20")
    void RESP_01_test_01(){
        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        int size = catalogue.getCatalogueSize();

        assertEquals(20, size);


    }
    @Test
    @DisplayName("Check library catalogue for valid book - Great Gatsby.")
    void RESP_01_test_02(){

        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        Book book = catalogue.getBook(0);
        String title = book.getTitle();
        assertEquals("Great Gatsby",title);
    }

    @Test
    @DisplayName("Check all book statuses are set to available")
    void RESP_01_test_03(){
        InitializeLibrary init = new InitializeLibrary();
        Catalogue catalogue = init.initializeLibrary();

        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
            Book b = catalogue.getBook(i);
            assertEquals(BookStatus.AVAILABLE, b.getStatus());
        }
    }

    @Test
    @DisplayName("check initialized 3 borrower accounts")
    void RESP_02_test_01(){
        Users user = new InitializeUsers().initializeUsers();
        assertEquals(3, user.size());
    }

    @Test
    @DisplayName("Check all borrower accounts have zero borrowed books initially")
    void RESP_02_test_02(){
        Users users = new InitializeUsers().initializeUsers();

        int maxBorrowed = -1;
        for (int i = 0; i < users.size(); i++) {
            int c = users.get(i).getBorrowedCount();
            if (c > maxBorrowed) maxBorrowed = c;
        }
        assertEquals(0, maxBorrowed);
    }

    @Test
    @DisplayName("System checks for valid username and password")
    void RESP_03_test_01(){
        String simulatedInput = "CarlTY\n1234Happy\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        String out = simulatedOutput.toString();
        boolean success = out.contains("Login successful");
        assertTrue(success, "Expected successful login message in output");
    }
}
