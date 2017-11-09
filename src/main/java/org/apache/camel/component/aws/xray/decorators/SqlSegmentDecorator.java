/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws.xray.decorators;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Namespace;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.sql.SqlEndpoint;

public class SqlSegmentDecorator extends AbstractSegmentDecorator {

    public static final String CAMEL_SQL_QUERY = "CamelSqlQuery";

    @Override
    public String getComponent() {
        return "sql";
    }

    @Override
    public void pre(Entity segment, Exchange exchange, Endpoint endpoint) {
        super.pre(segment, exchange, endpoint);

        segment.setNamespace(Namespace.REMOTE.toString());

        String url = "unknown";
        String user = "unknown";
        String driverVersion = "unknown";
        String dbType = "unknown";
        String dbVersion = "unknown";

        if (endpoint instanceof SqlEndpoint) {
            try {
                SqlEndpoint sqlEndpoint = (SqlEndpoint) endpoint;
                if (null != sqlEndpoint.getJdbcTemplate()) {
                    DataSource ds = sqlEndpoint.getJdbcTemplate().getDataSource();
                    Connection con = ds.getConnection();
                    DatabaseMetaData metaData = con.getMetaData();

                    url = metaData.getURL();
                    user = metaData.getUserName();
                    driverVersion = metaData.getDriverVersion();
                    dbType = metaData.getDatabaseProductName();
                    dbVersion = metaData.getDatabaseProductVersion();
                }
            } catch (SQLException sqlEx) {
                segment.addException(sqlEx);
            }
        }

        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("url", url);
        additionalParams.put("user", user);
        additionalParams.put("driver_version", driverVersion);
        additionalParams.put("database_type", dbType);
        additionalParams.put("database_version", dbVersion);

        Object sqlQuery = exchange.getIn().getHeader(CAMEL_SQL_QUERY);
        if (sqlQuery != null && sqlQuery instanceof String) {
            segment.putMetadata("query", sqlQuery);
        } else {
            URI uri = URI.create(endpoint.getEndpointUri());
            String query = uri.getAuthority();
            if (null != query) {
                segment.putMetadata("query", query);
            }
        }
        segment.putAllSql(additionalParams);
    }
}
