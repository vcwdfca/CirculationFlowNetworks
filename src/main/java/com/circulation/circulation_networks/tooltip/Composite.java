package com.circulation.circulation_networks.tooltip;

import com.circulation.circulation_networks.utils.CI18n;
//? if <1.20
import com.github.bsideup.jabel.Desugar;

import java.util.function.Supplier;

//? if <1.20
@Desugar
public record Composite(String key, Supplier<Object[]> supplier) implements LocalizedComponent {
    @Override
    public String get() {
        return CI18n.format(key, supplier.get());
    }
}
