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
package ch.kinet.ldap;

import javax.naming.directory.Attributes;

public class SearchResult {

    private final Attributes attributes;
    private final Name dn;

    public SearchResult(javax.naming.directory.SearchResult searchResult) {
        attributes = searchResult.getAttributes();
        dn = Name.parse(searchResult.getNameInNamespace());
    }

    public Name dn() {
        return dn;
    }

    public Attributes attributes() {
        return attributes;
    }
}
