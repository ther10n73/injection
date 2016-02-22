package ru.nojs.inject;

import org.reflections.Reflections;

import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Юыху on 13.02.2016.
 */
public class ContainerImpl implements Container {
    private Map<Class, Object> singeltonInstance = new HashMap<>();
    private Reflections reflection;

    ContainerImpl(String classPath){
        reflection = new Reflections();
    }

    public <T> T getInstance(Class<T> clazz){
        return clazz.isAnnotationPresent(Singleton.class) ? getSingelton(clazz) : createObject(clazz);
    }

    public <T> T getInstance(final String name, Class<T> requiredType) {
        if (requiredType.isInterface()) {
            Class named = reflection.getSubTypesOf(requiredType).stream()
                    .filter(c -> c.isAnnotationPresent(Named.class) && c.getAnnotation(Named.class).value().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Can't get instance"));
            return (T) getInstance(named);
        } else {
            return getInstance(requiredType);
        }
    }

    public Object getInstance(String name) {
        return null;
    }

    private <T> T createObject(Class<T> clazz){
        try{
             Constructor<T>[] constructor = (Constructor<T>[]) clazz.getConstructors();
            switch (constructor.length){
                case 0: throw new IllegalStateException("Can't create constructor");
                case 1: return createObject(constructor[0]);
                    default: {
                        return Stream.of(constructor)
                                .filter(c -> c.isAnnotationPresent(Inject.class))
                                .findFirst().map(this::createObject)
                                .orElseThrow(() -> new IllegalStateException("Can't get constructor"));
                    }
            }
        } catch (Exception e){
            throw new IllegalStateException("Can't create instance", e);
        }
    }

    private <T> T createObject (Constructor<T> constructor){
        try {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            } else {
                Parameter[] params = constructor.getParameters();
                List<Object> param = new ArrayList<>(params.length);
                Stream.of(params)
                        .forEach(parameter -> {
                            if (constructor.isAnnotationPresent(Named.class)){
                                param.add(getInstance(constructor.getAnnotation(Named.class).value(), parameter.getType()));
                            } else {
                                param.add(getInstance(parameter.getType()));
                            }
                        });

                return constructor.newInstance(param.toArray());
            }
        } catch (Exception e){
            throw  new IllegalStateException("Can't create instance");
        }
    }

    private <T> T getSingelton(Class<T> clazz){
        return (T)singeltonInstance.computeIfAbsent(clazz,(c) -> createObject(clazz));
    }
}
