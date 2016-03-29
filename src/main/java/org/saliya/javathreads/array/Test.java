package org.saliya.javathreads.array;

public class Test {
    public static void main(String[] args) {
        for (int i = 0; i < 100; ++i){
            System.out.println(i + " " + ((i&1) == 0 ? "even" : "odd"));
        }
    }
}
