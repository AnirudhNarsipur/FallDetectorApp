package com.anirudh.falldetect;
import java.util.ArrayDeque;

public class CircularFifo<E> extends ArrayDeque {
private final int MAX_SIZE ;
    private boolean locked ;
    CircularFifo(int max_size) {
    super(max_size);
    this.MAX_SIZE = max_size;
    this.locked = false;

}
    public synchronized void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public int getMAX_SIZE() {
        return this.MAX_SIZE;
    }

    public  synchronized boolean addelement(E e) {
        if(super.add(e)) {
            while (this.size() > MAX_SIZE && ( !isLocked()  )      ){

                this.removeFirst();
            }
            return true;

        }
       return false;
    }


    @Override
    public synchronized E removeFirst() {
        return (E)super.removeFirst();
    }
}
