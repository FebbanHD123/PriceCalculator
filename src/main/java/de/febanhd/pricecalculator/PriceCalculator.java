package de.febanhd.pricecalculator;

import de.febanhd.pricecalculator.command.PriceCalculatorCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PriceCalculator extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("pricecalculator").setExecutor(new PriceCalculatorCommand());
    }
}
