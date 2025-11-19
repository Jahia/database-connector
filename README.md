<a href="https://www.jahia.com/">
    <img src="https://www.jahia.com/modules/jahiacom-templates/images/jahia-3x.png" alt="Jahia logo" title="Jahia" align="right" height="60" />
</a>

# Database Connector

This module is required to use elasticsearch-connector or elasticsearch-connector-7 depending on your version of Jahia.

#### Setting up a connection

You can set up a connection by going to Administration -> Configuration -> Database connector. Click on "plus" button and 
fill out the form.

#### Running End-to-End tests

You can execute End-to-End tests locally by navigating to the tests folder and executing `docker-compose up -d`. This will start and set up Jahia, Elastic Search and cypress containers.
The cypress container will exit once it's done executing the tests but the Jahia and Elastic Search containers will remain.
At this point you can run the tests locally by executing `yarn e2e` or if you'd like to have the Cypress interface `yarn e2e:debug`
## Open-Source

This is an Open-Source module, you can find more details about Open-Source @ Jahia [in this repository](https://github.com/Jahia/open-source).
