package com.anirudh.falldetect;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

public class CircularFifo<E> extends ArrayDeque {
private final int MAX_SIZE ;

    CircularFifo(int max_size) {
    super(max_size);
    this.MAX_SIZE = max_size;

}
    public  synchronized boolean addElement(E e) {
        if(super.add(e)) {
            while (this.size() > MAX_SIZE  ){

                this.removeFirst();
            }
            return true;

        }
       return false;
    }


    @Override
    public synchronized E removeFirst() {
        if(this.isEmpty()){
            throw new NoSuchElementException();
        }
        return (E)super.removeFirst();
    }
}
