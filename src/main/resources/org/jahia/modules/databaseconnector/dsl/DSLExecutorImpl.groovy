package org.jahia.modules.databaseConnector.dsl

class DSLExecutorImpl implements DSLExecutor {

    @Override
    Map<String, Map> execute(URL filename, DSLHandler handler, Map <String, Map> importedConnectionsResults) {
        String scriptSource = filename.openStream().getText()
        Binding binding = new Binding();
        binding.setProperty("importedConnectionsResults", importedConnectionsResults)

        Script script = new GroovyShell().parse(scriptSource)
        script.setBinding(binding);

        // We use the metaclass method and property resolution to route calls to the handler.

        // Route missing methods to the handler.
        script.metaClass.methodMissing = { String name, args ->
            MetaMethod method = handler.metaClass.getMetaMethod(name, args)
            if (method) {
                method.invoke(handler, args)
            } else {
                throw new MissingMethodException("No method $name found", handler.class, args)
            }
        }

        // route missing properties to the handler
        script.metaClass.propertyMissing = { String name, value ->
            MetaProperty property = handler.metaClass.getMetaProperty(name)
            if (property) {
                return property.getProperty(handler)
            } else {
                throw new MissingPropertyException("No property $name found", handler.class)
            }
        }

        script.run()

        return importedConnectionsResults;
    }
}