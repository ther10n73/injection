package ru.nojs.inject;

import org.reflections.Reflections;

import javax.inject.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Kharitonov Oleg on 13.02.2016.
 */
public class ContainerImpl implements Container {
    private Map<Class, Object> singletonInstance = new HashMap<>();
    private final Reflections reflection;
    private ContainerGraph graph = new ContainerGraph();

    public ContainerImpl(String classPackage){
        reflection = new Reflections(classPackage);
    }

    public <T> T getInstance(Class<T> clazz){
        return clazz.isAnnotationPresent(Singleton.class) ? getSingleton(clazz) : createObject(clazz);
    }

    public <T> T getInstance(final String name, Class<T> requiredType) {
        if (requiredType.isInterface()) {
            Class named = reflection.getSubTypesOf(requiredType).stream()
                    .filter(c -> c.isAnnotationPresent(Named.class) && c.getAnnotation(Named.class).value().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Class must have annotation @Named"));
            return (T) getInstance(named);
        } else {
            return getInstance(requiredType);
        }
    }

    public Object getInstance(String name) {
        return null;
    }

    private <T> T createObject(Class<T> clazz) {
        Constructor<T>[] constructor = (Constructor<T>[]) clazz.getConstructors();
        switch (constructor.length){
            case 0: throw new IllegalStateException("Class don't have any constructor");
            case 1: return createObject(constructor[0]);
            default: {
                        return Stream.of(constructor)
                                .filter(c -> c.isAnnotationPresent(Inject.class))
                                .findFirst().map(this::createObject)
                                .orElseThrow(() -> new IllegalArgumentException("Constructor must have annotation @inject"));
                    }
            }
    }

    private <T> T createObject (Constructor<T> constructor){
        graph.add(constructor.getDeclaringClass());
        try {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            } else {
                Parameter[] params = constructor.getParameters();
                List<Object> param = new ArrayList<>(params.length);
                Stream.of(params)
                        .forEach(parameter -> {
                            graph.add(parameter.getType());
                            if (graph.isClassReuse(constructor.getDeclaringClass(), parameter.getType())){
                                throw new IllegalArgumentException("This is circular dependency");
                            }  else {
                                graph.add(constructor.getDeclaringClass(), parameter.getType());
                            }
                            if (parameter.isAnnotationPresent(Named.class)){
                                param.add(getInstance(parameter.getAnnotation(Named.class).value(), parameter.getType()));
                            } else {
                                param.add(getInstance(parameter.getType()));
                            }
                        });

                return constructor.newInstance(param.toArray());
            }
        } catch (Exception e){
            throw  new IllegalStateException("Can't create instance", e);
        }
    }

    private <T> T getSingleton(Class<T> clazz){
        return (T) singletonInstance.computeIfAbsent(clazz,(c) -> createObject(clazz));
    }
}
