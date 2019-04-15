package priv.bigant.test;

import java.util.Stack;

public class StackTest {

    public static void main(String[] args) {
        Stack stack = new Stack();
        stack.push("");
        System.out.println(stack.pop());
        System.out.println(stack.pop());
    }
}
