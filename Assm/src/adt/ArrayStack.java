/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author Chia Min Yi
 */
public class ArrayStack<T> implements StackInterface<T> {

    private T[] stack;
    private int index;
    private static final int DEFAULT_CAPACITY = 50;
    
    public ArrayStack(){
        this(DEFAULT_CAPACITY);
    }
    public ArrayStack(int capacity){
        stack = (T[]) new Object[capacity];
        index = -1;
    }
    @Override
    public void push(T newEntry) {
        if (index == stack.length - 1){
            System.out.println("Stack is full! You are not allowed to add new task.");
            return;
        }
            stack[++index] = newEntry;
    }

    @Override
    public T pop() {
        if(isEmpty()){
            System.out.println("Stack is empty!");
            return null;
        }
        T topItem = stack[index];
        stack[index] = null;
        index--;
        
        return topItem;
    }

    @Override
    public T peek() {
        if (isEmpty()){
            return null;
        }
        return stack[index];
    }

    @Override
    public boolean isEmpty() {
        return index == -1;
    }

    @Override
    public int size() {
        return index + 1;
    }

    @Override
    public void clear() {
        while (!isEmpty()){
            pop();
        }
    }  
}
