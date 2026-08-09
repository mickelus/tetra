package se.mickelus.tetra.items.toolbelt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Runtime bridge for Baubles-compatible implementations that omit setPlayer. */
public final class BaublesCompatibility {
    private static final ConcurrentMap<Class<?>, Method> SET_PLAYER = new ConcurrentHashMap<>();
    private static final Set<Class<?>> WITHOUT_SET_PLAYER =
            Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

    private BaublesCompatibility() { }

    public static void setPlayerIfSupported(Object handler, Object player) {
        Class<?> handlerClass = handler.getClass();
        Method method = SET_PLAYER.get(handlerClass);

        if (method == null && !WITHOUT_SET_PLAYER.contains(handlerClass)) {
            method = resolve(handlerClass, player.getClass().getClassLoader());
            if (method == null) {
                WITHOUT_SET_PLAYER.add(handlerClass);
            } else {
                SET_PLAYER.put(handlerClass, method);
            }
        }

        if (method != null) {
            try {
                method.invoke(handler, player);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to invoke Baubles setPlayer", e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw new RuntimeException("Baubles setPlayer failed", cause);
            }
        }
    }

    private static Method resolve(Class<?> handlerClass, ClassLoader loader) {
        try {
            Class<?> livingBase = Class.forName(
                    "net.minecraft.entity.EntityLivingBase",
                    false,
                    loader
            );
            try {
                return handlerClass.getMethod("setPlayer", livingBase);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Unable to resolve EntityLivingBase for Baubles compatibility",
                    e
            );
        }
    }
}
