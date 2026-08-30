package me.marin.lockout.lockout.goal.config;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GoalPoolConfig {

    private static final Path CONFIG_PATH = new File("./config/goal-pool.yml").toPath();

    private static final Yaml YAML;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        YAML = new Yaml(options);
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createConfigDir();
            save();
        } else {
            try {
                String content = Files.readString(CONFIG_PATH);
                Map<String, Object> yamlData = YAML.load(content);
                
                if (yamlData != null) {
                    // Parse the YAML data into goal states
                    List<GoalBuilder<?,?>> allGoals = GoalRegistry.INSTANCE.getRegisteredGoals();
                    int enabledCount = 0;
                    int disabledCount = 0;
                    boolean configUpdated = false;
                    
                    for (GoalBuilder<?,?> goal : allGoals) {
                        Object value = yamlData.get(goal.getStaticId());
                        if (value instanceof Boolean) {
                            boolean enabled = (Boolean) value;
                            goal.setEnabled(enabled);
                        } else {
                            configUpdated = true;
                        }
                        if (goal.isEnabled()) {
                            enabledCount++;
                        } else {
                            disabledCount++;
                        }
                    }
                    
                    Lockout.log("GoalPoolConfig loaded: " + enabledCount + " enabled, " + disabledCount + " disabled goals");
                    
                    if (configUpdated) {
                        Lockout.log("New goals detected in config, updating file...");
                        save();
                    }
                }
            } catch (Exception e) {
                Lockout.log("Invalid goal-pool.yml file, using default values.");
                Lockout.error(e);
            }
        }
    }

    private static void createConfigDir() {
        try {
            Files.createDirectories(Path.of("./config"));
        } catch (Exception e) {
            Lockout.error(e);
        }
    }

    public static void save() {
        try {
            String yamlContent = generateYamlContent();
            Files.writeString(CONFIG_PATH, yamlContent);
        } catch (Exception e) {
            Lockout.error(e);
        }
    }

    private static String generateYamlContent() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("# Lockout Goal Pool Configuration\n");
        yaml.append("# Edit this file to enable/disable goals in the random pool\n");
        yaml.append("# Set to 'true' to enable, 'false' to disable\n");
        yaml.append("# Goals marked as 'false' will not appear in randomly generated boards\n\n");

        // Group goals by category for better organization
        Map<GoalCategory, List<GoalBuilder<?,?>>> categorizedGoals = categorizeGoals();
        
        for (Map.Entry<GoalCategory, List<GoalBuilder<?,?>>> entry : categorizedGoals.entrySet()) {
            String category = entry.getKey().getName();
            List<GoalBuilder<?,?>> goals = entry.getValue();
            
            yaml.append("# ").append(category).append("\n");
            for (GoalBuilder<?,?> goal : goals) {
                yaml.append(goal.getStaticId()).append(": ").append(goal.isEnabled()).append("\n");
            }
            yaml.append("\n");
        }
        
        return yaml.toString();
    }

    private static Map<GoalCategory, List<GoalBuilder<?,?>>> categorizeGoals() {
        Map<GoalCategory, List<GoalBuilder<?,?>>> categories = new LinkedHashMap<>();

        for(GoalBuilder<?,?> goal : GoalRegistry.INSTANCE.getRegisteredGoals()) {
            if(!categories.containsKey(goal.getCategory())) {
                categories.put(goal.getCategory(), new ArrayList<>(List.of(goal)));
            } else {
                categories.get(goal.getCategory()).add(goal);
            }
        }
        
        return categories;
    }
}
