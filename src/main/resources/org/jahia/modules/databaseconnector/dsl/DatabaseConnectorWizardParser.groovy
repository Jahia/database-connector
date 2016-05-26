package org.jahia.modules.databaseConnector.dsl

class DatabaseConnectorWizardParser extends GroovyObjectSupport {
    Map<String, Object> contentMap = new LinkedHashMap<String, Object>();

    public Object invokeMethod(String name, Object args) {
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            Object[] arr = (Object[]) args;
            if (arr.length == 1) {
                if (arr[0] instanceof Closure) {
                    def parser = new DatabaseConnectorWizardParser()
                    def cl = (Closure) arr[0]
                    def code = cl.rehydrate(parser, this, this)
                    code.resolveStrategy = Closure.DELEGATE_FIRST
                    code()
                    return setAndGetContent(name, parser.contentMap);
                } else if (arr[0] instanceof Map) {
                    return setAndGetContent(name, arr[0]);
                } else if (arr[0] instanceof String) {
                    return setAndGetContent(name, arr[0]);
                } else if (arr[0] instanceof Collection) {
                    return setAndGetContent(name, arr[0]);
                }
            } else {
                return setAndGetContent(name, arr.collect({ it ->
                    if (it instanceof Closure) {
                        def parser = new DatabaseConnectorWizardParser()
                        def code = it.rehydrate(parser, this, this)
                        code.resolveStrategy = Closure.DELEGATE_FIRST
                        code()
                        return parser.contentMap;
                    } else {
                        it
                    }
                }))
            }
        } else {
            return setAndGetContent(name, new HashMap<String, Object>());
        }
    }

    private Object setAndGetContent(String name, Object value) {
        contentMap.put(name, value);
        return contentMap;
    }

    public String toString() {
        return contentMap.toMapString();
    }
}