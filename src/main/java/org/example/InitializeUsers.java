package org.example;

public class InitializeUsers {
    public Users initializeUsers() {
        Users users = new Users();
        users.add(new Borrow("alice", "pass123"));
        users.add(new Borrow("bob",   "pass456"));
        users.add(new Borrow("charlie", "pass789"));
        return users;
    }
}
