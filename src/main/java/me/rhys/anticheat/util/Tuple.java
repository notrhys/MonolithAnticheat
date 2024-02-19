package me.rhys.anticheat.util;

public class Tuple<K, V>
{
    private K one;
    private V two;
    
    public Tuple(K one, V two) {
        this.one = one;
        this.two = two;
    }
    
    public K getOne() {
        return this.one;
    }
    
    public V getTwo() {
        return this.two;
    }
    
    public void setOne(K one) {
        this.one = one;
    }
    
    public void setTwo(V two) {
        this.two = two;
    }
}
