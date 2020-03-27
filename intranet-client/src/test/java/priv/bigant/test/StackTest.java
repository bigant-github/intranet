package priv.bigant.test;

import java.util.Stack;

public class StackTest {

    public static void main(String[] args) {
        Stack stack = new Stack();
        stack.push("1");
        stack.push("2");
        System.out.println(stack.pop() + "     " + stack.size());
        System.out.println(stack.pop() + "     " + stack.size());
    }
}
