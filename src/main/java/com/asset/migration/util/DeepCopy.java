package com.asset.migration.util;


import org.springframework.data.util.Pair;

import java.lang.reflect.Method;
import java.util.*;

public class DeepCopy<T, U> {
    T source;
    U destination;
    Set<Pair<String, String>> exceptions;



    public DeepCopy(T source, U destination){
        this.source = source;
        this.destination = destination;
    }

    public void setException(String from, String to){
        if (exceptions == null) exceptions = new HashSet<>();
        exceptions.add(Pair.of(from, to));
    }

    public U copy(){
        try {
            Class clsSource = this.source.getClass();
            Class clsDestination = destination.getClass();
            destination = (U) clsDestination.getDeclaredConstructor().newInstance();

            Method[] methods = clsSource.getMethods();

            for (Method method:methods){
                if (method.getName().startsWith("get")){
                    try{
                        Object val = method.invoke(this.source);
                        if (val == null) continue;
                        String methodName = getMethodName(method.getName());

                        Class valClass = getValClass(val);

                        Method methodDestination = clsDestination.getMethod(methodName, valClass);
                        methodDestination.invoke(destination, val);
                    }catch (Exception ex){
                        System.out.println(ex);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return destination;

    }

    private Class getValClass(Object val){
        Class  valClass = val.getClass();
        if (val instanceof ArrayList){
            valClass = List.class;
        }
        return valClass;
    }

    private String getMethodName(String methodName){
        boolean hasExepection = false;
        if (exceptions != null){
            for(Pair exception : exceptions){
                if (exception.getFirst().equals(methodName)) {
                    hasExepection = true;
                    methodName = (String) exception.getSecond();
                }
            }
        }
        if (!hasExepection) methodName =methodName.replace("get", "set");
        return methodName;
    }

}
