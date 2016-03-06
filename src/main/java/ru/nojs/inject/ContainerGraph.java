package ru.nojs.inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Юыху on 05.03.2016.
 */
public class ContainerGraph<V> {
    private Map<V, List<V>> map = new HashMap<>();

    public void add (V clazz){
        if (map.containsKey(clazz)) return;
        map.put(clazz, new ArrayList<>());
    }

    public void add (V clazzFrom, V clazzTo){
        this.add(clazzFrom);
        this.add(clazzTo);
        map.get(clazzFrom).add(clazzTo);
    }

    public boolean isCircular(V clazzTo, V clazzFrom){
        if (clazzTo.equals(clazzFrom)){
            return true;
        }

        for (V v : map.get(clazzFrom)){
            if (isCircular(clazzTo, v)){
                return true;
            }
        }
        return  false;
    }
}
