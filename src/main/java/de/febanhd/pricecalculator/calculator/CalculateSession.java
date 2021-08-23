package de.febanhd.pricecalculator.calculator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import de.febanhd.pricecalculator.PriceCalculator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CalculateSession {

    private final HashMap<Material, Double> PRICES;
    private final List<Material> NOT_CALCULATED_MATERIALS = Lists.newArrayList();
    private final List<Material> materialsToRemove = Lists.newArrayList();

    public CalculateSession(HashMap<Material, Double> prices) {
        PRICES = prices;
    }

    public void start() {
        PRICES.put(Material.AIR, 0D);
        for (Material value : Material.values()) {
            if (value != Material.AIR && !value.name().startsWith("LEGACY"))
                NOT_CALCULATED_MATERIALS.add(value);
        }
        int i = 1;
        do {
            i++;
            for (Material not_calculated_material : NOT_CALCULATED_MATERIALS) {
                List<Recipe> recipes = Lists.newArrayList();
                Bukkit.recipeIterator().forEachRemaining(recipe -> {
                    if (recipe.getResult().getType().equals(not_calculated_material))
                        recipes.add(recipe);
                });
                for (Recipe recipe : recipes) {
                    Map<Material, Integer> materials = Maps.newHashMap();
                    if (recipe instanceof ShapedRecipe) {
                        ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                        for (String shape : shapedRecipe.getShape()) {
                            for (char c : shape.toCharArray()) {
                                if (shapedRecipe.getIngredientMap().get(c) != null) {
                                    Material material = shapedRecipe.getIngredientMap().get(c).getType();
                                    if (materials.containsKey(material))
                                        materials.put(material, materials.get(material) + 1);
                                    else
                                        materials.put(material, 1);
                                }
                            }
                        }
                    }
                    if (recipe instanceof ShapelessRecipe) {
                        ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                        for (RecipeChoice recipeChoice : shapelessRecipe.getChoiceList()) {
                            for (Material material : PRICES.keySet()) {
                                if (recipeChoice.test(new ItemStack(material))) {
                                    if (materials.containsKey(material))
                                        materials.put(material, materials.get(material) + 1);
                                    else
                                        materials.put(material, 1);
                                }
                            }
                        }
                    }
                    if (recipe instanceof CookingRecipe<?>) {
                        CookingRecipe<?> cookingRecipe = (CookingRecipe<?>) recipe;
                        for (Material material : PRICES.keySet()) {
                            if (cookingRecipe.getInputChoice().test(new ItemStack(material))) {
                                if (materials.containsKey(material))
                                    materials.put(material, materials.get(material) + 1);
                                else
                                    materials.put(material, 1);
                            }
                        }
                    }
                    AtomicDouble price = new AtomicDouble(0D);
                    AtomicBoolean available = new AtomicBoolean(true);
                    if (!materials.isEmpty()) {
                        materials.forEach((material, integer) -> {
                            if (this.PRICES.containsKey(material)) {
                                price.addAndGet(this.PRICES.get(material) * integer / recipe.getResult().getAmount());
                            } else {
                                available.set(false);
                            }
                        });
                    } else
                        available.set(false);
                    if (available.get()) {
                        double finalPrice = price.get();
                        if (PRICES.containsKey(not_calculated_material) && finalPrice > PRICES.get(not_calculated_material))
                            finalPrice = PRICES.get(not_calculated_material);
                        PRICES.put(not_calculated_material, finalPrice);
                        this.materialsToRemove.add(not_calculated_material);
                        System.out.println("Set price for " + not_calculated_material.name() + " to " + price);
                    }
                }
            }
            this.NOT_CALCULATED_MATERIALS.removeAll(materialsToRemove);
            if (materialsToRemove.isEmpty() || NOT_CALCULATED_MATERIALS.isEmpty()) {
                String id = UUID.randomUUID().toString().split("-")[0];
                try {
                    safeInFile(id);
                    System.out.println("Calculated " + PRICES.size() + " prices. Saved in file: prices#" + id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            this.materialsToRemove.clear();
        } while (true);
    }

    private void safeInFile(String id) throws IOException {
        if (!PriceCalculator.getPlugin(PriceCalculator.class).getDataFolder().exists()) {
            PriceCalculator.getPlugin(PriceCalculator.class).getDataFolder().mkdir();
        }
        File file = new File(PriceCalculator.getPlugin(PriceCalculator.class).getDataFolder(), "prices#" + id + ".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        StringBuilder sb = new StringBuilder();
        this.PRICES.forEach((material, price) -> {
            sb.append(material.name().toLowerCase(Locale.ROOT)).append(": ").append(price);
            sb.append("\n");
        });
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
