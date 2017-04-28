package org.jahia.modules.databaseConnector.serialization.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author alexander karmanov on 2017-04-28.
 */
public class AllDirectives {

    private List<Directives> directives;

    public AllDirectives(List<Directives> directives) {
        this.directives = directives;
    }

    public List<Directives> getDirectives() {
        return directives;
    }

    public void setDirectives(List<Directives> directives) {
        this.directives = directives;
    }

    @JsonIgnore
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonForm = mapper.valueToTree(this);
        return jsonForm != null ? jsonForm.toString() : null;
    }
}
