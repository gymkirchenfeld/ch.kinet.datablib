/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet.sql;

import ch.kinet.Util;
import ch.kinet.reflect.MetaObject;
import ch.kinet.reflect.Property;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StatementBuilder<T> {

    private final List<Statement.BoundParameterSetter> boundParameterSetters;
    private final List<String> columnNames;
    private final Connection connection;
    private final MetaObject<T> metaObject;
    private final Map<String, ParameterSetter> parameterSetters;
    private final String schemaName;
    private final StringBuilder sql;
    private int nextParameterIndex;

    static String sqlName(String name) {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!first) {
                    result.append("_");
                }

                result.append(Character.toLowerCase(c));
            }
            else {
                result.append(c);
            }

            first = false;
        }

        return result.toString();
    }

    StatementBuilder(Connection connection, String schemaName, Class<T> targetClass) {
        this.boundParameterSetters = new ArrayList<>();
        this.columnNames = new ArrayList<>();
        this.connection = connection;
        this.metaObject = MetaObject.forClass(targetClass);
        this.parameterSetters = new HashMap<>();
        this.schemaName = schemaName;
        this.sql = new StringBuilder();
        this.nextParameterIndex = 1;
    }

    final void appendBoundParameter(String propertyName, Object value) {
        final Property property = metaObject.property(propertyName);
        final ParameterSetter setter = addParameter(property);
        boundParameterSetters.add(new Statement.BoundParameterSetter(setter, value));
        append("?");
    }

    final void appendFieldName(String propertyName) {
        final Property property = metaObject().property(propertyName);
        final StringBuilder result = new StringBuilder();
        result.append(property.getName());
        final Property keyProperty = property.getType().keyProperty();
        if (keyProperty != null) {
            result.append(keyProperty.getName());
        }

        append(sqlName(result.toString()));
    }

    final List<Statement.BoundParameterSetter> boundParameterSetters() {
        return boundParameterSetters;
    }

    final Connection connection() {
        return connection;
    }

    final MetaObject<T> metaObject() {
        return metaObject;
    }

    final Map<String, ParameterSetter> parameterSetters() {
        return parameterSetters;
    }

    final String sql() {
        return sql.toString();
    }

    protected final void addPropertySetter(Property property) {
        parameterSetters.put(property.getName(), addParameter(property));
    }

    protected final void append(String value) {
        sql.append(value);
    }

    protected final void appendTableName() {
        if (!Util.isEmpty(schemaName)) {
            append(schemaName);
            append(".");
        }

        append(sqlName(metaObject().getName()));
    }

    protected final void appendWhereClause(Condition where) {
        if (where != null) {
            append(" where ");
            where.visit(this);
        }
    }

    protected final List<String> columnNames() {
        return columnNames;
    }

    protected final String schemaName() {
        return schemaName;
    }

    protected final String sequenceName(String propertyName) {
        final StringBuilder result = new StringBuilder();
        if (!Util.isEmpty(schemaName())) {
            result.append(schemaName());
            result.append(".");
        }

        result.append(sqlName(metaObject().getName() + propertyName));
        return result.toString();
    }

    private ParameterSetter addParameter(Property property) {
        return addParameter(property, sqlName(property.getName()));
    }

    private ParameterSetter addParameter(Property property, String columnName) {
        final Property key = property.getType().keyProperty();
        if (key == null) {
            return addSimpleParameter(property, columnName);
        }
        else {
            return addSingleKeyLookupParameter(property, key);
        }
    }

    private ParameterSetter addSingleKeyLookupParameter(Property property, Property key) {
        final String columnName = sqlName(property.getName() + key.getName());
        final ParameterSetter keySetter = addParameter(key, columnName);
        return ParameterSetter.createSingleKey(connection, property, keySetter);

    }

    private ParameterSetter addSimpleParameter(Property property, String columnName) {
        final ParameterSetter setter = ParameterSetter.createSimple(connection, property, nextParameterIndex);
        ++nextParameterIndex;
        columnNames.add(columnName);
        return setter;
    }
}
