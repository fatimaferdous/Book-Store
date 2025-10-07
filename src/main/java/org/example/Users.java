package org.example;

import java.util.ArrayList;

public class Users {
    ArrayList<Borrow> borrowed = new ArrayList<>();
    int size() { return borrowed.size(); }
    public void add(Borrow b) { borrowed.add(b); }
    Borrow get(int index) { return borrowed.get(index); }
}
