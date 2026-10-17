package com.harukiyu.clarityskills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class YamlValidationTest {

    @Test
    public void testAllYamlFilesAreValid() throws Exception {
        Path resourcesPath = Paths.get("src", "main", "resources");
        Yaml yaml = new Yaml();
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            List<Path> ymlFiles = paths.filter(p -> p.toString().endsWith(".yml")).toList();
            Assertions.assertFalse(ymlFiles.isEmpty(), "No YAML files found in resources directory!");

            for (Path path : ymlFiles) {
                try (InputStream in = Files.newInputStream(path)) {
                    Object parsed = yaml.load(in);
                    Assertions.assertNotNull(parsed, "Failed to parse YAML file: " + path.getFileName());
                    Assertions.assertInstanceOf(Map.class, parsed, "YAML root must be a map: " + path.getFileName());
                }
            }
        }
    }

    @Test
    public void testMiniMessageTooltipResolution() {
        MiniMessage mm = MiniMessage.miniMessage();
        Component t1Comp = mm.deserialize("<gold>Tingkat Blaze</gold>");
        Component t2Comp = mm.deserialize("<red><bold>Tingkat Inferno</bold></red>");

        String template = "<gray>• <t1_name> (Syarat Lvl <t1_req>)</gray>\n<gray>• <t2_name> (Syarat Lvl <t2_req>)</gray>";

        Component rendered = mm.deserialize(template,
                Placeholder.component("t1_name", t1Comp),
                Placeholder.unparsed("t1_req", "25"),
                Placeholder.component("t2_name", t2Comp),
                Placeholder.unparsed("t2_req", "50")
        );

        String serialized = mm.serialize(rendered);
        System.out.println("Rendered tooltip serialization: " + serialized);

        // Verify that <gold> and <red><bold> are parsed, NOT escaped as \<gold\> or \<red\>
        Assertions.assertFalse(serialized.contains("\\<gold\\>"), "Must not escape <gold>");
        Assertions.assertFalse(serialized.contains("\\<red\\>"), "Must not escape <red>");
        Assertions.assertTrue(serialized.contains("Tingkat Blaze"), "Must contain Tingkat Blaze");
        Assertions.assertTrue(serialized.contains("Tingkat Inferno"), "Must contain Tingkat Inferno");
    }
}
