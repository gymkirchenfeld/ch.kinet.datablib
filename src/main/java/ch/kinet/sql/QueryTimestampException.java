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

public class QueryTimestampException extends SqlException {

    QueryTimestampException(Throwable cause) {
        super(buildMessage(cause), cause);
    }

    private static String buildMessage(Throwable cause) {
        final StringBuilder result = new StringBuilder();
        result.append("Error while querying timestamp: ");
        result.append(cause.getMessage());
        return result.toString();
    }
}