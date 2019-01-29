package se.mickelus.tetra.blocks;

import com.google.common.collect.Maps;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Map;
import java.util.function.Predicate;

public class PropertyMatcher implements Predicate<IBlockState> {
    private final Map< IProperty<?>, Predicate<?>> propertyPredicates = Maps.newHashMap();
    @Override
    public boolean test(IBlockState blockState) {
        for (Map.Entry<IProperty<?>, Predicate<?>> entry : this.propertyPredicates.entrySet()) {
            if (!matches(blockState, (IProperty) entry.getKey(), (Predicate) entry.getValue()))
            {
                return false;
            }
        }

        return true;
    }

    protected <T extends Comparable<T>> boolean matches(IBlockState blockState, IProperty<T> property, Predicate<T> predicate) {
        return predicate.test(blockState.getValue(property));
    }

    public <V extends Comparable<V>> PropertyMatcher where(IProperty<V> property, Predicate<? extends V> is) {

        {
            this.propertyPredicates.put(property, is);
            return this;
        }
    }
}
