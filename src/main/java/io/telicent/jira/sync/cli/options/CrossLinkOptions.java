package io.telicent.jira.sync.cli.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.telicent.jira.sync.client.model.CrossLinks;

import java.io.File;
import java.io.IOException;

/**
 * Options relating to cross-links
 */
public class CrossLinkOptions {

    private final ObjectMapper mapper = new YAMLMapper();

    @Option(name = "--cross-links-file", title = "CrossLinksFile", description = "Provides a file used to store cross links")
    @com.github.rvesse.airline.annotations.restrictions.File
    @Required
    private File crossLinksFile;

    private CrossLinks instance = null;

    /**
     * Loads the cross-links
     *
     * @return Cross-links
     * @throws IOException Thrown if there's a problem loading the cross-links
     */
    public CrossLinks loadCrossLinks() throws IOException {
        if (this.instance != null) {
            return this.instance;
        }

        if (this.crossLinksFile.exists()) {
            this.instance = this.mapper.readValue(this.crossLinksFile, CrossLinks.class);
        } else {
            this.instance = new CrossLinks();
        }

        return this.instance;
    }

    /**
     * Saves the cross-links
     *
     * @throws IOException Thrown if there's a problem saving the cross-links
     */
    public void saveCrossLinks() throws IOException {
        if (this.instance == null) {
            return;
        }

        this.mapper.writeValue(this.crossLinksFile, this.instance);
    }
}
