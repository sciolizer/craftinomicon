package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.InitializingBean;
import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.metrics.Gauge;
import name.ball.joshua.craftinomicon.recipe.metrics.GaugeStat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker implements Listener, InitializingBean {

    @Inject Plugin plugin;
    @PermissionKey("craftinomicon.upgrade.announce") Permission upgradePermission;
    @Gauge(graph = "Errors", plotter = "Malformed URL Exception in UpdateChecker") GaugeStat malformedURLException;
    @Gauge(graph = "Errors", plotter = "Unparseable features") GaugeStat unparseableFeatures;
    @Gauge(graph = "Errors", plotter = "Error checking for updates") GaugeStat errorUpdating;
    @Gauge(graph = "UpdateChecker", plotter = "enabled") GaugeStat enabled;

    ReaderProvider readerProvider;
    private String thisVersionStr = null;
    private long[] thisVersion = null;

    AsynchronousRunnableExecutor asynchronousRunnableExecutor = new AsynchronousRunnableExecutor() {
        @Override
        public void runTaskAsynchronously(final Runnable runnable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTaskAsynchronously(plugin);
        }
    };

    public UpdateChecker() {
        final URL filesURL;
        try {
            filesURL = new URL("https://api.curseforge.com/servermods/files?projectIds=92152");
        } catch (MalformedURLException e) {
            malformedURLException.set(1);
            return;
        }
        final Charset utf8 = Charset.forName("UTF-8");
        readerProvider = new ReaderProvider() {
            @Override
            public Reader getReader() throws IOException {
                URLConnection urlConnection = filesURL.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.addRequestProperty("User-Agent", "craftinomicon/v" + thisVersionStr + " (by sciolizer)");
                urlConnection.setDoOutput(true); // todo: test in a functional test to see if this is actually necessary
                return new InputStreamReader(urlConnection.getInputStream(), utf8);
            }
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            thisVersionStr = plugin.getDescription().getVersion();
            thisVersion = parseVersion(thisVersionStr); // todo: functional test
        } catch (UnparseableVersionException e) {
            throw new RuntimeException(e);
        }
    }

    long lastUpgradeCheck = 0l;
    List<String> upgradeText = null;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        getUpdateText(event.getPlayer());
    }

    private long lastNonNullGetUpdateText = 0;

    public List<String> getUpdateText(Permissible player) {
        long yesterday = System.currentTimeMillis() - 1000l * 60 * 60 * 24;
        boolean isEnabled = lastNonNullGetUpdateText > yesterday;
        try {
            if (readerProvider == null) return null;
            if (!player.hasPermission(upgradePermission)) return null;
            lastNonNullGetUpdateText = System.currentTimeMillis();
            isEnabled = true;
            List<String> text;
            synchronized (this) {
                if (lastUpgradeCheck < yesterday) {
                    enabled.set(1);
                    lastUpgradeCheck = System.currentTimeMillis();
                    asynchronousRunnableExecutor.runTaskAsynchronously(new GetLatestVersion());
                } else {
                    enabled.set(0);
                }
                text = upgradeText;
            }
            return text;
        } finally {
            enabled.set(isEnabled ? 1 : 0);
        }
    }

    // simpler and more correct version of https://github.com/gravitylow/Updater
    // todo: functional test
    private class GetLatestVersion implements Runnable {

        @Override
        public void run() {
            boolean success = false;
            try {
                runThrowing();
                success = true;
            } catch (Exception e) {
                errorUpdating.set(1);
            }
            if (success) {
                errorUpdating.set(0);
            }
        }

        private void runThrowing() throws Exception {
            // See https://github.com/gravitylow/Updater for another approach. Consider supporting
            // auto-update, not only the announcements of updates.

            BufferedReader bufferedReader = new BufferedReader(readerProvider.getReader());
            Scanner scanner = new Scanner(bufferedReader);
            StringBuilder str = new StringBuilder();
            while (scanner.hasNextLine()) {
                str.append(scanner.nextLine());
                if (scanner.hasNextLine()) {
                    str.append('\n');
                }
            }

            JSONArray jsonArray = (JSONArray) JSONValue.parse(str.toString());
            List<APIFile> apiFiles = new ArrayList<APIFile>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                APIFile apiFile = new APIFile();
                apiFile.name = (String) jsonObject.get("name");
                apiFiles.add(apiFile);
            }

            // todo: log something in the finalizer of menu to make sure that they are getting garbage collected
            Set<PlannedFeature> newFeatures = new LinkedHashSet<PlannedFeature>();
            boolean updateAvailable = false;
            int unparseableVersionExceptions = 0;
            for (APIFile apiFile : apiFiles) {
                long[] v;
                try {
                    v = version(apiFile.name);
                } catch (UnparseableVersionException e) {
                    unparseableVersionExceptions++;
                    continue;
                }
                if (moreRecent(v)) {
                    updateAvailable = true;
                    newFeatures.addAll(features(v));
                }
            }
            unparseableFeatures.set(unparseableVersionExceptions);

            if (updateAvailable) {
                List<String> features = new ArrayList<String>();
                for (PlannedFeature newFeature : newFeatures) {
                    if (PlannedFeature.SHOW_SOMETHING.equals(newFeature) || PlannedFeature.ADDITIONAL_FEATURES.equals(newFeature)) continue;
                    features.add(newFeature.englishDescription);
                }
                if (newFeatures.contains(PlannedFeature.ADDITIONAL_FEATURES)) {
                    if (features.isEmpty()) {
                        features.add("New features!");
                    } else {
                        features.add(PlannedFeature.ADDITIONAL_FEATURES.englishDescription);
                    }
                }
                synchronized (UpdateChecker.this) {
                    upgradeText = features;
                }
            }
        }

        private Collection<? extends PlannedFeature> features(long[] version) {
            long code = version[3];
            if (!has(code, PlannedFeature.SHOW_SOMETHING)) {
                return Collections.emptyList();
            }
            List<PlannedFeature> result = new ArrayList<PlannedFeature>();
            for (PlannedFeature plannedFeature : PlannedFeature.values()) {
                if (PlannedFeature.SHOW_SOMETHING.equals(plannedFeature)) continue;
                if (has(code, plannedFeature)) {
                    result.add(plannedFeature);
                }
            }
            return result;
        }

        private boolean has(long code, PlannedFeature plannedFeature) {
            long mask = 1l << plannedFeature.bit;
            return (code & mask) == mask;
        }

        private boolean moreRecent(long[] candidateVersion) {
            for (int i = 0; i < 4; i++) {
                if (candidateVersion[i] > thisVersion[i]) {
                    return true;
                } else if (candidateVersion[i] < thisVersion[i]) {
                    return false;
                }
            }
            return false;
        }

    }

    @SuppressWarnings("UnusedDeclaration") // values set by gson
    private static class APIFile {
        private String name;
    }

    private long[] version(String name) throws UnparseableVersionException {
        Matcher matcher = JAR_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            String group = matcher.group(1);
            return parseVersion(group);
        } else {
            throw new UnparseableVersionException();
        }
    }

    private long[] parseVersion(String group) throws UnparseableVersionException {
        String[] split = group.split("\\.");
        if (split.length == 0) {
            throw new UnparseableVersionException(); // wut?
        }
        long[] result = new long[4];
        for (int i = 1; i <= 4; i++) {
            if (split.length < i) {
                break;
            }
            try {
                result[i - 1] = Long.parseLong(split[i - 1]);
            } catch (NumberFormatException e) {
                throw new UnparseableVersionException();
            }
        }
        return result;
    }

    private static final Pattern JAR_NAME_PATTERN = Pattern.compile("craftinomicon-([0-9\\.]+)\\.(jar|zip)");

    private static class UnparseableVersionException extends Exception {
    }

    // unit test to make sure all bits are distinct
    static enum PlannedFeature {
        SHOW_SOMETHING(0, ""),
        BUG_FIXES(1, "Bug fixes!"),
        ADDITIONAL_FEATURES(2, "Additional new features not listed here!"), // special case, changes to "New features", if no PlannedFeatures are marked; todo: iterate up to 64 beginning after the highest enum value. If any of those bits are set, assume ADDITIONAL_FEATURES
        POTIONS(5, "Potions!"),
        MOB_DROPS(6, "Mob drops!"),
        MATERIAL_HARDNESS(7, "Minimum pickaxe level required!"),
        ORE_DENSITY(8, "Ore density percentages!"),
        ORE_DEPTH(9, "Depth range in which ores spawn!"),
        BIOME(10, "Biome and structures in which items are found!"),
        INSTA_CRAFTING(11, "Automatic crafting!"),
        CRAFTABILITY(12, "Tells you whether you can craft the item with what's in your inventory!"),
        BANNERS(13, "Banner patterns!"),
        FIREWORKS(14, "Fireworks!"),
        ENCHANTMENT_TABLE(15, "Enchantment table probabilities!"),
        USER_SPECIFIC_LANGUAGE(16, "Option to display in each user's own language!"), // this could get complicated if any of our lore contains the name of actual minecraft items
        DYED_LEATHER(17, "Dyed leather patterns!"),
        MULTIPLE_SORT_ORDERS(18, "Multiple sorting options!"),
        TEXT_SEARCH(19, "Search by name!"),
        REPAIR_COST(20, "Cost to repair (in an anvil)!"),
        NEW_PERMISSIONS(21, "New permission settings!"),
        LANGUAGE_BRAZILIAN_PORTUGUESE(22, "Brazilian Portuguese language option!"),
        LANGUAGE_FRENCH(23, "French language option!"),
        LANGAUGE_GERMAN(24, "German language option!"),
        LANGUAGE_ITALIAN(25, "Italian language option!"),
        LANGUAGE_JAPANESE(26, "Japanese language option!"),
        LANGUAGE_KOREAN(27, "Korean language option!"),
        LANGUAGE_LATIN_AMERICAN_SPANISH(28, "Latin American Spanish language option!"),
        LANGUAGE_POLISH(29, "Polish language option!"),
        LANGUAGE_RUSSIAN(30, "Russian language option!"),
        LANGUAGE_SPANISH(31, "Spanish language option!"),
        AUTOMATIC_UPGRADES(32, "Automatic plugin upgrades!"),
        UI_IMPROVEMENTS(33, "UI improvements");

        final int bit;
        private final String englishDescription;

        PlannedFeature(int bit, String englishDescription) {
            this.bit = bit;
            this.englishDescription = englishDescription;
        }
    }

    interface AsynchronousRunnableExecutor {
        void runTaskAsynchronously(Runnable runnable);
    }

    interface ReaderProvider {
        Reader getReader() throws IOException;
    }
}
