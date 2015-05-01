package name.ball.joshua.craftinomicon;

import name.ball.joshua.craftinomicon.recipe.RecipeSnapshotTest;
import name.ball.joshua.craftinomicon.recipe.Setup;
import name.ball.joshua.craftinomicon.recipe.Test;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CraftinomiconTestRunner implements Listener {

    private static final UUID JOSHUA_BALL_UUID = UUID.fromString("13572363-c7dd-497e-a516-9bdcf156d5a2");

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!JOSHUA_BALL_UUID.equals(player.getUniqueId())) {
            return;
        }

        for (Class<?> testClass : Arrays.<Class<?>>asList(RecipeSnapshotTest.class)) {
            Method setupMethod = null;
            Method[] methods = testClass.getMethods();
            List<Method> testMethods = new ArrayList<Method>(methods.length);
            for (Method method : methods) {
                if (method.isAnnotationPresent(Setup.class)) {
                    if (setupMethod != null) {
                        throw new RuntimeException("cannot have two setup methods!");
                    }
                    setupMethod = method;
                } else if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                }
            }
            Bukkit.getServer().broadcastMessage("testMethods.size() = " + testMethods.size());

            for (Method testMethod : testMethods) {
                Object o = testClass.newInstance();
                if (setupMethod != null) {
                    setupMethod.invoke(o);
                }
                testMethod.invoke(o);
                Bukkit.getServer().broadcastMessage("ran test");
            }
        }
        Bukkit.getServer().broadcastMessage("thought I ran some tests");
    }
}
