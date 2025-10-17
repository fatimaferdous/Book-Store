package org.example;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Library {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        PrintWriter output = new PrintWriter(System.out, true);

        Users users = new Users();
        Borrow user1 = new Borrow("fatima", "pass1234");
        users.add(user1);

        InitializeLibrary init = new InitializeLibrary();
        Catalogue catalogue = init.initializeLibrary();

        AuthSystem auth = new AuthSystem(users);

        output.println("Welcome to the Library Management System!");
        auth.promptLogin(input, output);

        boolean running = true;
        while (running) {
            Borrow currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                auth.promptLogin(input, output);
                continue;
            }


            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    auth.displayAllBooks(catalogue, output);
                    Book selectedBook = auth.borrowBook(catalogue, input, output);
                    auth.confirmBorrowing(selectedBook, input, output);
                    if (selectedBook != null) {
                        auth.recordBorrowTransaction(currentUser, selectedBook, auth.calculateDueDate());
                        auth.updateAccountStatus(currentUser, selectedBook);
                        auth.finalConfirmation(currentUser, selectedBook, auth.calculateDueDate(), input, output);
                    }
                    break;

                case "2":
                    Book toReturn = auth.selectReturnBook(currentUser, input, output);
                    if (toReturn != null) {
                        auth.checkPendingHolds(toReturn, output);
                        auth.holdUpdate(currentUser, toReturn);
                        auth.returnConfirmation(toReturn, input, output);
                    }
                    break;

                case "3":
                    auth.logout(input, output);
                    break;
            }
        }
    }

}