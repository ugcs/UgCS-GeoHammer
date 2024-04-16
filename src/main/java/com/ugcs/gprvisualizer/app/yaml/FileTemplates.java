package com.ugcs.gprvisualizer.app.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import com.ugcs.gprvisualizer.app.yaml.Template.FileType;

@Component
public class FileTemplates implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(FileTemplates.class);

    private static final String TEMPLATES_FOLDER = "templates";

    private final List<Template> templates = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("Loading templates...");

        Constructor c = new Constructor(Template.class, new LoaderOptions());

        c.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                if (name.indexOf('-') > -1) {
                    name = toCameCase(name);
                }
                return super.getProperty(type, name);
            }

            private String toCameCase(String name) {
                String[] parts = name.split("-");
                StringBuilder sb = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    sb.append(StringUtils.capitalize(parts[i]));
                }
                return sb.toString();
            }
        });

        c.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(c);

        loadTemplates(yaml, TEMPLATES_FOLDER, templates);
    }

    private void loadTemplates(Yaml yaml, String path, List<Template> templates) {
        try {
            // Get all resources ending with .yaml from path
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("file:" + path + "/*.yaml");

            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    try {
                        Template template = yaml.load(inputStream);
                        if (template.isTemplateValid()) {
                            templates.add(template);
                            logger.debug("Valid template, data: " + template);
                        } else {
                            logger.error("Invalid template: " + template);
                        }
                    } catch (YAMLException e) {
                        logger.error("Error reading template: " + e.getMessage());
                    }
                }
            }
            if (templates.isEmpty()) {
                logger.error("No templates found in " + path);
            }
        } catch (IOException e) {
            logger.error("Error reading template: " + e.getMessage());
        }
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Template findTemplate(List<Template> templates, String fileName) {
        if (fileName.endsWith(".sgy")) {
            var ot = templates.stream()
                    .filter(t -> FileType.Segy.equals(t.getFileType()))
                    .findFirst();
            return ot.isPresent() ? ot.get() : null;
        }

        String firstNonEmptyLines = "";
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            List<String> firstTenLines = lines.limit(10).collect(Collectors.toList());
            firstNonEmptyLines = String.join(System.lineSeparator(), firstTenLines);
            logger.debug(firstNonEmptyLines);
        } catch (IOException e) {
            logger.error("Error reading file: " + e.getMessage());
        }

        for (var t : templates) {
            try {
                var regex = Pattern.compile(t.getMatchRegex(), Pattern.MULTILINE | Pattern.DOTALL);
                if (regex.matcher(firstNonEmptyLines).find()) {
                    return t;
                }
            } catch (Exception e) {
                logger.error("Error matching template: " + e.getMessage());
            }
        }

        return null;
    }

    private Template createSegyTemplate() {
        return new Template() {
            @Override
            public String getCode() {
                return "Segy";
            }

            @Override
            public FileType getFileType() {
                return FileType.Segy;
            }

            @Override
            public String getName() {
                return "Segy";
            }
        };
    }
}
