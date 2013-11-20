package org.jahia.modules.databaseConnector.webflow.model.mongo;

import org.jahia.modules.databaseConnector.mongo.MongoDatabaseConnection;

/**
 * Date: 11/20/2013
 *
 * @author Frédéric Pierre
 * @version 1.0
 */
public interface MongoConnection extends MongoDatabaseConnection {

    public void setWriteConcern(String writeConcernName);

}
