package se.mickelus.tetra;

import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class ReflectionHelper {
    /**
     * Finds a method with the specified name and parameters in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodName     The name of the method to find (used in developer environments, i.e. "getWorldTime").
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param parameterTypes The parameter types of the method to find.
     * @return The method with the specified name and parameters in the given class.
     */
    @Nonnull
    public static Method findMethod(@Nonnull Class<?> clazz, @Nonnull String methodName, @Nullable String methodObfName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(StringUtils.isNotEmpty(methodName), "Method name cannot be empty");

        String nameToFind;
        if (methodObfName == null || (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
            nameToFind = methodName;
        } else {
            nameToFind = methodObfName;
        }

        do {
            try {
                Method m = clazz.getDeclaredMethod(nameToFind, parameterTypes);
                m.setAccessible(true);
                return m;
            } catch (Exception e) {
                clazz = clazz.getSuperclass();
            }
        } while (clazz != null);

        throw new NoSuchMethodException(String.format("No such method: %s (%s)", methodName, methodObfName));
    }
}
