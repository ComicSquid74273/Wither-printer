package com.bonggerz.witherprinter;

import com.bonggerz.witherprinter.modules.WitherPrinter;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public final class WitherPrinterAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOG.info("Initializing Wither Printer");
        Modules.get().add(new WitherPrinter());
    }

    @Override
    public String getPackage() {
        return "com.bonggerz.witherprinter";
    }
}
