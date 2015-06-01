package name.ball.joshua.craftinomicon.recipe;

import com.google.gson.Gson;
import name.ball.joshua.craftinomicon.recipe.metrics.GaugeStat;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UpdateCheckerTest {
    
    private Gson gson = new Gson();
    @Mock private Plugin plugin;
    @Mock private Permission upgradePermission;
    @Mock private GaugeStat malformedURLException;
    @Mock private GaugeStat unparseableFeatures;
    @Mock private GaugeStat errorUpdating;
    @Mock private GaugeStat enabled;

    private UpdateChecker updateChecker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        updateChecker = new UpdateChecker() {{
            gson = UpdateCheckerTest.this.gson;
            plugin = UpdateCheckerTest.this.plugin;
            upgradePermission = UpdateCheckerTest.this.upgradePermission;
            malformedURLException = UpdateCheckerTest.this.malformedURLException;
            unparseableFeatures = UpdateCheckerTest.this.unparseableFeatures;
            errorUpdating = UpdateCheckerTest.this.errorUpdating;
            enabled = UpdateCheckerTest.this.enabled;
        } };

        updateChecker.asynchronousRunnableExecutor = new UpdateChecker.AsynchronousRunnableExecutor() {
            @Override
            public void runTaskAsynchronously(Runnable runnable) {
                runnable.run();
            }
        };
    }

    @org.junit.Test
    public void testNoPermission() throws Exception {
        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(false);
        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertNull(updateText);
        verify(enabled).set(0);
    }

    @Test
    public void testRecentlyUpdated() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.2"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        updateChecker.lastUpgradeCheck = System.currentTimeMillis() - 1000l * 60 * 60 * 12;
        updateChecker.upgradeText = new ArrayList<String>();
        updateChecker.upgradeText.add("hello");

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(1, updateText.size());
        assertEquals("hello", updateText.get(0));
    }

    @Test
    public void testNeedsToUpdate() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.1"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        setApiResponse("0.2");

        updateChecker.afterPropertiesSet();

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(0, updateText.size());
    }

    @Test
    public void testUpdateContainsFrenchAndGerman() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.2"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        setApiResponse("0.2.1.25165825");

        updateChecker.afterPropertiesSet();

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(2, updateText.size());
        assertEquals("French language option!", updateText.get(0));
        assertEquals("German language option!", updateText.get(1));
    }

    @Test
    public void testUpdateContainsOnlyUnpredictedFeatures() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.2"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        setApiResponse("0.2.1.5");

        updateChecker.afterPropertiesSet();

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(1, updateText.size());
        assertEquals("New features!", updateText.get(0));
    }

    @Test
    public void testUpdateContainsBugFixesAndUnpredictedFeatures() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.2"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        setApiResponse("0.2.1.7");

        updateChecker.afterPropertiesSet();

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(2, updateText.size());
        assertEquals("Bug fixes!", updateText.get(0));
        assertEquals("Additional new features not listed here!", updateText.get(1));
    }

    @Test
    public void testUpdateContainsPotionsAndUnpredictedFeatures() throws Exception {
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(new StringReader("name: craftinomicon\nmain: name.ball.joshua.craftinomicon.Craftinomicon\nversion: 0.2"));
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        Permissible permissible = mock(Permissible.class);
        when(permissible.hasPermission(any(Permission.class))).thenReturn(true);

        setApiResponse("0.2.1.37");

        updateChecker.afterPropertiesSet();

        List<String> updateText = updateChecker.getUpdateText(permissible);
        assertEquals(2, updateText.size());
        assertEquals("Potions!", updateText.get(0));
        assertEquals("Additional new features not listed here!", updateText.get(1));
    }

    @Test
    public void testPlannedFeaturesAllDistinct() throws Exception {
        Set<Integer> bits = new LinkedHashSet<Integer>();
        UpdateChecker.PlannedFeature[] plannedFeatures = UpdateChecker.PlannedFeature.values();
        for (UpdateChecker.PlannedFeature plannedFeature : plannedFeatures) {
            bits.add(plannedFeature.bit);
        }
        assertEquals(plannedFeatures.length, bits.size());
    }

    private void setApiResponse(String version) {
        final String json = "[{\"downloadUrl\":\"http:\\/\\/servermods.cursecdn.com\\/files\\/879\\/834\\/craftinomicon-0.1.jar\",\"fileName\":\"craftinomicon-0.1.jar\",\"fileUrl\":\"http:\\/\\/www.curseforge.com\\/files\\/879834\",\"gameVersion\":\"1.8.3\",\"md5\":\"b867722cf89d71269b3e215aac78af40\",\"name\":\"craftinomicon-0.1.jar\",\"projectId\":92152,\"releaseType\":\"beta\"},{\"downloadUrl\":\"http:\\/\\/servermods.cursecdn.com\\/files\\/879\\/948\\/craftinomicon-" + version + ".jar\",\"fileName\":\"craftinomicon-" + version + ".jar\",\"fileUrl\":\"http:\\/\\/www.curseforge.com\\/files\\/879948\",\"gameVersion\":\"1.8.3\",\"md5\":\"b466141d35398362a4826947869ccd58\",\"name\":\"craftinomicon-" + version + ".jar\",\"projectId\":92152,\"releaseType\":\"beta\"}]";
        updateChecker.readerProvider = new UpdateChecker.ReaderProvider() {
            @Override
            public Reader getReader() throws IOException {
                return new StringReader(json);
            }
        };
    }

    private static final String EXAMPLE_API_JSON = "[{\"downloadUrl\":\"http:\\/\\/servermods.cursecdn.com\\/files\\/879\\/834\\/craftinomicon-0.1.jar\",\"fileName\":\"craftinomicon-0.1.jar\",\"fileUrl\":\"http:\\/\\/www.curseforge.com\\/files\\/879834\",\"gameVersion\":\"1.8.3\",\"md5\":\"b867722cf89d71269b3e215aac78af40\",\"name\":\"craftinomicon-0.1.jar\",\"projectId\":92152,\"releaseType\":\"beta\"},{\"downloadUrl\":\"http:\\/\\/servermods.cursecdn.com\\/files\\/879\\/948\\/craftinomicon-0.2.jar\",\"fileName\":\"craftinomicon-0.2.jar\",\"fileUrl\":\"http:\\/\\/www.curseforge.com\\/files\\/879948\",\"gameVersion\":\"1.8.3\",\"md5\":\"b466141d35398362a4826947869ccd58\",\"name\":\"craftinomicon-0.2.jar\",\"projectId\":92152,\"releaseType\":\"beta\"}]";

}
