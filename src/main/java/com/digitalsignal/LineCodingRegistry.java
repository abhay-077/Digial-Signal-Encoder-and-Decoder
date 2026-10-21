package com.digitalsignal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class LineCodingRegistry {

    private static final Map<String, Supplier<LineCodingStrategy>> FACTORIES =
            Map.of(
                "NRZ-L", NrzLStrategy::new,
                "NRZ-I", NrzIStrategy::new,
                "Manchester", ManchesterStrategy::new,
                "Differential Manchester", DifferentialManchesterStrategy::new,
                "AMI", AmiStrategy::new,
                "B8ZS", B8zsStrategy::new,
                "HDB3", Hdb3Strategy::new,
                "RZ", RzStrategy::new
            );

    private LineCodingRegistry() {}

    public static Optional<LineCodingStrategy> find(String name) {
        Supplier<LineCodingStrategy> factory = FACTORIES.get(name);
        return factory == null
                ? Optional.empty()
                : Optional.of(factory.get());
    }

    public static Set<String> schemes() {
        return FACTORIES.keySet();
    }
}
