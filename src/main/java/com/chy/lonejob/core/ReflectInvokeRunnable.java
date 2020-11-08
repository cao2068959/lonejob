package com.chy.lonejob.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectInvokeRunnable implements Runnable {

    private Method method;
    private Object core;

    public ReflectInvokeRunnable(Method method, Object core) {
        this.method = method;
        this.core = core;
        method.setAccessible(true);
    }

    @Override
    public void run() {
        try {
            method.invoke(core);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
