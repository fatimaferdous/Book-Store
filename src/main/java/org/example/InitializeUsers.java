package org.example;

public class InitializeUsers {
    public Users initializeUsers() {
        Users users = new Users();
        users.add(new Borrow("CarlTY", "1234Happy"));
        users.add(new Borrow("Unix",   "teyrhupf"));
        users.add(new Borrow("HannahT", "CarletonU"));
        return users;
    }
}
