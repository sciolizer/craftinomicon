package name.ball.joshua.craftinomicon;

import name.ball.joshua.craftinomicon.recipe.RecipeSnapshotTest;
import name.ball.joshua.craftinomicon.recipe.Setup;
import name.ball.joshua.craftinomicon.recipe.Test;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftinomiconTestRunner {

	public void runTests() {
        try {
            runTestsThrowing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void runTestsThrowing() throws Exception {
        try {
            // todo: maybe run this check not on the main thread? it might require a dns lookup, which is slow
            if (!"kenodayroll-lm".equals(InetAddress.getLocalHost().getHostName())) {
                return;
            }
        } catch (UnknownHostException e) {
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
