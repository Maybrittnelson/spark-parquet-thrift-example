package com.geshaofeng.scala;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyList<E> {

    private List<E> list;

    public MyList(List<E> list) {
        this.list = list;
    }

    public Set<E> toSet() {
        HashSet<E> set = new HashSet<>(list);
        return set;
    }

    public static void main(String[] args) {
/*        ArrayList<Integer> its = new ArrayList<>();
        its.add(1);
        its.add(1);

        System.out.println(new MyList<>(its).toSet());*/
    }
}
