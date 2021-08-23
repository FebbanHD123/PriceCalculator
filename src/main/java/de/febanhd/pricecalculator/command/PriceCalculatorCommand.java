package de.febanhd.pricecalculator.command;

import com.google.common.collect.Maps;
import de.febanhd.pricecalculator.calculator.CalculateSession;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PriceCalculatorCommand implements CommandExecutor {

    private HashMap<Material, Double> PRICES = Maps.newHashMap();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(sender.hasPermission("pricecalculator.use")) {
            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("setprice")) {
                    String priceString = args[1];
                    if(!isDouble(priceString)) {
                        player.sendMessage("§cPrice must be a double");
                        return false;
                    }
                    double price = Double.parseDouble(priceString);
                    Material material = player.getInventory().getItemInMainHand().getType();
                    if(material == null || material.equals(Material.AIR)) {
                        player.sendMessage("§cYou must hold an item in your hand");
                        return false;
                    }
                    PRICES.put(material, price);
                    player.sendMessage("§7Set price for §e" + material.name() + " §7to §a" + price);
                }
            }else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("start")) {
                    new CalculateSession(Maps.newHashMap(this.PRICES)).start();
                }
            }
        }
        return false;
    }

    private boolean isDouble(String in) {
        try {
            Double.parseDouble(in);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    //pricecalculator setprice <double>
}
