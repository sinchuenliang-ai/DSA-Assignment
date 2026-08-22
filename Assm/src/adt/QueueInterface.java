/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package adt;

/**
 *
 * @author Sin Chuen Liang
 */
public interface QueueInterface<T> {
    void enqueue(T newEntry);
    T dequeue();
    T getFront();
    boolean isEmpty();
    void clear();
    int size();
}
