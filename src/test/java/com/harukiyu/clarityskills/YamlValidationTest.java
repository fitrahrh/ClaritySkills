package com.harukiyu.clarityskills;

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
}
