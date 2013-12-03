package org.jahia.modules.databaseConnector.webflow.model;

import org.jahia.modules.databaseConnector.ConnectionData;
import org.jahia.modules.databaseConnector.DatabaseConnectorManager;
import org.jahia.modules.databaseConnector.DatabaseTypes;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Date: 11/5/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public abstract class AbstractConnection implements Connection {

    private static final long serialVersionUID = 1L;

    protected String id;

    protected String oldId;

    protected String host;

    protected Integer port;

    protected String dbName;

    protected String uri;

    protected String user;

    protected String password;

    protected DatabaseTypes databaseType;

    private transient MessageContext messages;

    public AbstractConnection(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    public AbstractConnection(ConnectionData connectionData) {
        this.id = connectionData.getId();
        this.oldId = id;
        this.host = connectionData.getHost();
        this.port = connectionData.getPort();
        this.dbName = connectionData.getDbName();
        this.uri = connectionData.getUri();
        this.user = connectionData.getUser();
        this.password = connectionData.getPassword();
        this.databaseType = connectionData.getDatabaseType();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getOldId() {
        return oldId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public DatabaseTypes getDatabaseType() {
        return databaseType;
    }

    @Override
    public void setDatabaseType(DatabaseTypes databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    public String getDisplayName() {
        return databaseType.getDisplayName();
    }

    public final void validateEnterConfig(ValidationContext context) {
        messages = context.getMessageContext();
        if (id == null || id.isEmpty()) {
            addRequiredErrorMessage("id");
        }
        else if (oldId == null || !oldId.equals(id)) {
            if (!DatabaseConnectorManager.getInstance().isAvailableId(id, databaseType)) {
                addErrorMessage("id", "dc_databaseConnector.label.port.message.idAlreadyInUse");
            }
        }
        validateEnterConfig();
    }

    protected abstract void validateEnterConfig();

    private String getMessage(String key) {
        return Messages.get("resources.database-connector", key, LocaleContextHolder.getLocale());
    }

    private String getMessage(String key, Object... args) {
        return Messages.getWithArgs("resources.database-connector", key, LocaleContextHolder.getLocale(), args);
    }

    private String getRequiredMessage(String propertyName) {
        String arg = getMessage("dc_databaseConnector.label."+propertyName);
        return getMessage("dc_databaseConnector.label.message.required", arg);
    }

    protected void addErrorMessage(String source, String key) {
        messages.addMessage(new MessageBuilder().error().source(source).defaultText(getMessage(key)).build());
    }

    protected void addErrorMessage(String source, String key, Object... args) {
        messages.addMessage(new MessageBuilder().error().source(source).defaultText(getMessage(key, args)).build());
    }

    protected void addRequiredErrorMessage(String source) {
        messages.addMessage(new MessageBuilder().error().source(source).defaultText(getRequiredMessage(source)).build());
    }

}
