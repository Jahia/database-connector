package org.jahia.modules.databaseConnector.serialization.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author alexander karmanov on 2017-04-28.
 */
public class Directives {
    private String label;
    private Directive connectionDirective;
    private List<Directives> statusDirectives;

    public Directives(String label, Directive connectionDirective, List<Directives> statusDirectives) {
        this.label = label;
        this.connectionDirective = connectionDirective;
        this.statusDirectives = statusDirectives;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Directive getConnectionDirective() {
        return connectionDirective;
    }

    public void setConnectionDirective(Directive connectionDirective) {
        this.connectionDirective = connectionDirective;
    }

    public List<Directives> getStatusDirectives() {
        return statusDirectives;
    }

    public void setStatusDirectives(List<Directives> statusDirectives) {
        this.statusDirectives = statusDirectives;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonForm = mapper.valueToTree(this);
        return jsonForm != null ? jsonForm.toString() : null;
    }
}
