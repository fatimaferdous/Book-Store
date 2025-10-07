package org.example;

import java.util.ArrayList;

public class Users {
    ArrayList<Borrow> borrowed = new ArrayList<>();
    int size() { return borrowed.size(); }
    Borrow get(int index) { return borrowed.get(index); }
}
