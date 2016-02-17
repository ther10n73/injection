package ru.nojs.inject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Юыху on 13.02.2016.
 */
public class ContainerImpl implements Container {
    private Map<Class, Object> singeltonInstance = new HashMap<>();

    ContainerImpl(String classPath){

    }

    public <T> T getInstance(Class<T> clazz){
        return clazz.isAnnotationPresent(Singleton.class) ? getSingelton(clazz) : createObject(clazz);
    }

    public <T> T getInstance(String name, Class<T> requiredType) {
        return null;
    }

    public Object getInstance(String name) {
        return null;
    }

    private <T> T createObject(Class<T> clazz){
        try{
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception e){
            throw new IllegalStateException("Can't create instance", e);
        }
    }

    private <T> T getSingelton(Class<T> clazz){
        return (T)singeltonInstance.computeIfAbsent(clazz,(c) -> createObject(clazz));
    }
}
