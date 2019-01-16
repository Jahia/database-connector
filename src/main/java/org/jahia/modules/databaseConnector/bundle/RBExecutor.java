/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.databaseConnector.bundle;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by stefan on 2017-06-07.
 */
public class RBExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RBExecutor.class);
    private JCRTemplate jcrTemplate;

    public RBExecutor(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void addRBDictionnaryToAngularConfigFile(JahiaUser user, final Locale locale, final String filePath, final JahiaTemplatesPackage jahiaTemplatesPackage) throws RepositoryException, IOException {
        jcrTemplate.doExecuteWithSystemSessionAsUser(user, Constants.EDIT_WORKSPACE, locale, new JCRCallback<Object>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                File jsFile = new File(filePath);
                try {
                    FileWriter fw = new FileWriter(jsFile.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    if (jahiaTemplatesPackage != null) {
                        ResourceBundle resourceBundle = ResourceBundles.get(jahiaTemplatesPackage, locale);
                        Enumeration<String> keyEnum = resourceBundle.getKeys();
                        List<String> keys = new LinkedList<String>();
                        while (keyEnum.hasMoreElements()) {
                            keys.add(keyEnum.nextElement());
                        }
                        Collections.sort(keys);
                        bw.write("(function() {");
                        bw.newLine();
                        bw.write("\t 'use strict';");
                        bw.newLine();
                        bw.write("\t _.extend(dci18n, {");
                        bw.newLine();
                        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                            String key = iterator.next();
                            String value = getValue(resourceBundle, key);

                            if (value != null) {
                                bw.write("\"");
                                bw.write(processKey(key));
                                bw.write("\"");
                                bw.write(":\"");
                                bw.write(processValue(value));
                                bw.write("\"");
                                if (iterator.hasNext()) {
                                    bw.write(",");
                                }
                                bw.newLine();
                            }
                        }
                        bw.write("});");
                        bw.newLine();
                        bw.write("})();");
                    }
                    bw.close();
                } catch (IOException e) {
                    logger.error("Failed to write to buffer: " + e.getMessage() + "\n" + e);
                }
                return null;
            }
        });
    }

    private String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() * 2);
        int sz = value.length();
        for (int i = 0; i < sz; i++) {
            char ch = value.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.append("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.append("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.append("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.append('\\');
                        out.append('b');
                        break;
                    case '\n':
                        out.append('\\');
                        out.append('n');
                        break;
                    case '\t':
                        out.append('\\');
                        out.append('t');
                        break;
                    case '\f':
                        out.append('\\');
                        out.append('f');
                        break;
                    case '\r':
                        out.append('\\');
                        out.append('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            out.append("\\u00" + hex(ch));
                        } else {
                            out.append("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        out.append('\\');
                        out.append('\'');
                        break;
                    case '"':
                        out.append('\\');
                        out.append('"');
                        break;
                    case '\\':
                        out.append('\\');
                        out.append('\\');
                        break;
                    case '/':
                        out.append('\\');
                        out.append('/');
                        break;
                    default:
                        out.append(ch);
                        break;
                }
            }
        }

        return out.toString();
    }

    private String getValue(ResourceBundle bundle, String key) {
        String value = null;
        try {
            value = bundle.getString(key);
        } catch (MissingResourceException e) {
            // ignore
        }
        return value;
    }

    private String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }

    private String processKey(String key) {
        return key;
    }

    private String processValue(String value) {
        return escape(value);
    }

}
