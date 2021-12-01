package se.mickelus.tetra.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class HexCodec implements PrimitiveCodec<Integer> {
    public static final HexCodec instance = new HexCodec();

    @Override
    public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
        return ops
                .getStringValue(input)
                .map(val -> (int) Long.parseLong(val, 16));
    }

    @Override
    public <T> T write(final DynamicOps<T> ops, final Integer value) {
        return ops.createString(Integer.toHexString(value));
    }

    @Override
    public String toString() {
        return "tetra-hex";
    }
}
