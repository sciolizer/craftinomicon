package name.ball.joshua.craftinomicon;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.*;
import name.ball.joshua.craftinomicon.recipe.i18n.Translation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CraftinomiconCommandExecutor implements CommandExecutor {

    @Inject private BrowserScreenFactory browserScreenFactory;
    @Inject private MaterialDataSubstitutes materialDataSubstitutes;
    @Inject private RecipeBookChecker recipeBookChecker;
    @Inject private RecipeBrowser recipeBrowser;
    @Inject private RecipeScreenFactory recipeScreenFactory;
    @Inject private RecipeSnapshot recipeSnapshot;
    @PermissionKey("craftinomicon.craft.book") private Permission craftingPermission;
    @PermissionKey("craftinomicon.command.enabled") private Permission enabled;
    @PermissionKey("craftinomicon.command.bookless") private Permission bookless;
    @Translation(value = "title", english = "Craftinomicon") String craftinomicon;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof HumanEntity)) {
            if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
                sender.sendMessage(craftinomicon + " has a GUI, and so cannot be used from a console.");
            } else {
                sender.sendMessage("You do not appear to have an inventory, and so I cannot show you the " + craftinomicon + ".");
            }
            return true;
        }
        HumanEntity player = (HumanEntity) sender;
        if (!player.hasPermission(enabled)) {
            sender.sendMessage(craftinomicon + " is disabled.");
            return true;
        }
        if (!player.hasPermission(bookless)) {
            boolean found = false;
            for (ItemStack craftinomicon : player.getInventory().getContents()) {
                if (recipeBookChecker.isRecipeBook(craftinomicon)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String message = "You must have the " + craftinomicon + " in your inventory.";
                if (player.hasPermission(craftingPermission)) {
                    message += " Create one by placing a book and a crafting table into a crafting table.";
                }
                sender.sendMessage(message);
                return true;
            }
        }
        if (args == null || args.length == 0) {
            recipeBrowser.showAllItems(player);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        String joinedArgs = sb.toString();
        Material material = Material.matchMaterial(joinedArgs);
        if (material == null) {
            try {
                material = Bukkit.getUnsafe().getMaterialFromInternalName(joinedArgs);
            } catch (Exception e) { }
        }
        List<MaterialData> choices = new ArrayList<MaterialData>();
        if (material != null && !Material.AIR.equals(material)) {
            choices.addAll(materialDataSubstitutes.get(new ItemStack(material).getData()));
        } else {
            String filtered = joinedArgs.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
            for (MaterialData materialData : recipeSnapshot.getAllMaterialsInAtLeastOneRecipe()) {
                if (materialData.getItemType().toString().contains(filtered)) {
                    choices.add(materialData);
                }
            }
        }
        if (choices.isEmpty()) {
            sender.sendMessage("No matches.");
            return true;
        }
        if (choices.size() == 1) {
            // skip browser screen and go straight to recipe screen; union the recipes with the usages
            MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(choices.get(0));
            Set<MaterialRecipes.IconifiedRecipe> recipesWithIcons = materialRecipes.getRecipesWithIcons();
            Set<MaterialRecipes.IconifiedRecipe> usagesWithIcons = materialRecipes.getUsagesWithIcons();
            List<MaterialRecipes.IconifiedRecipe> iconifiedRecipes = new ArrayList<MaterialRecipes.IconifiedRecipe>(recipesWithIcons.size() + usagesWithIcons.size());
            iconifiedRecipes.addAll(recipesWithIcons);
            iconifiedRecipes.addAll(usagesWithIcons);
            if (iconifiedRecipes.size() == 0) {
                sender.sendMessage("No known recipes for that.");
                return true;
            }
            recipeBrowser.showScreen(player, recipeScreenFactory.newRecipeScreen(iconifiedRecipes, 0));
            return true;
        }
        recipeBrowser.showScreen(player, browserScreenFactory.newBrowserScreen(0, player, new LinkedHashSet<MaterialData>(choices)));
        return true;
    }
}
