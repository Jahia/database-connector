package org.jahia.modules.databaseConnector.mongo.initializer;

import com.mongodb.WriteConcern;
import org.apache.commons.lang.WordUtils;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public class WriteConcernChoiceListInitializer implements ModuleChoiceListInitializer {

    private String key = "mongoWriteConcern";

    private final static List<ChoiceListValue> choiceListValues = initChoiceListValues();

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    private static List<ChoiceListValue> initChoiceListValues() {
        List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();
        for (Field f : WriteConcern.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(WriteConcern.class)) {
                String name = f.getName();
                choiceListValues.add(new ChoiceListValue(WordUtils.capitalizeFully(name.replace("_", " ").toLowerCase()), name));
            }
        }
        return choiceListValues;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
                                                     Locale locale, Map<String, Object> context) {
        return choiceListValues;
    }
}
