package org.example;

import java.util.ArrayList;

public class Users {
    ArrayList<Borrow> borrowed = new ArrayList<>();
    int size() { return borrowed.size(); }
    public void add(Borrow b) { borrowed.add(b); }
    Borrow get(int index) { return borrowed.get(index); }

    /*public Borrow findByUsername(String username) {
        for (Borrow b : borrowed) {
            if (b.getUsername().equals(username)) {
                return b;
            }
        }
        return null;
    }*/
}
