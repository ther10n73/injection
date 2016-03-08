package ru.nojs.inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kharitonov Oleg on 05.03.2016.
 */
public class ContainerGraph {
    private Map<Class, List<Class>> map = new HashMap<>();

    public void add (Class clazz){
        if (map.containsKey(clazz)) return;
        map.put(clazz, new ArrayList<>());
    }

    public void add (Class clazzFrom, Class clazzTo){
        this.add(clazzFrom);
        this.add(clazzTo);
        map.get(clazzFrom).add(clazzTo);
    }

    public boolean isClassReuse(Class clazzTo, Class clazzFrom){
        if (clazzTo.equals(clazzFrom)){
            return true;
        }

        for (Class v : map.get(clazzFrom)){
            if (isClassReuse(clazzTo, v)){
                return true;
            }
        }
        return  false;
    }
}
