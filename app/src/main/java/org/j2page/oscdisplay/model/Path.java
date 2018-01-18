package org.j2page.oscdisplay.model;

/**
 * Model object for an OSC path with associated HTML template.
 */

public class Path {
    private String address;
    private Template template;
    private boolean updateTemplate;

    public Path(String address, Template template) {
        this.address = address;
        this.template = template;
    }

    public Path(String address, Template template, boolean update) {
        this(address, template);
        this.updateTemplate = update;
    }

    public String getAddress() {
        return address;
    }

    public Template getTemplate() {
        return template;
    }

    public boolean isUpdateTemplate() {
        return updateTemplate;
    }
}
