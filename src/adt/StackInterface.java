/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author LENOVO
 */
public interface StackInterface<T> {

    void push(T newEntry);
    T pop();
    T peek();
    boolean isEmpty();
    int size();
    void clear();
}

