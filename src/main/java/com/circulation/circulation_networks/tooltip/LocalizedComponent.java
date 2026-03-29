package com.circulation.circulation_networks.tooltip;

import com.circulation.circulation_networks.utils.CI18n;

import java.util.function.Supplier;

public interface LocalizedComponent extends Supplier<String> {

    String get();

    static LocalizedComponent of(String key) {
        return () -> CI18n.format(key);
    }

    static LocalizedComponent withArgs(String key, Supplier<Object[]> args) {
        return new Composite(key, args);
    }

    static LocalizedComponent withTranslatedArg(String key, String argKey) {
        return () -> CI18n.format(key, CI18n.format(argKey));
    }

    static LocalizedComponent description(String key) {
        return () -> "§7" + CI18n.format(key);
    }

    static LocalizedComponent blank() {
        return () -> "";
    }

    static LocalizedComponent literal(String text) {
        return () -> text;
    }
}
