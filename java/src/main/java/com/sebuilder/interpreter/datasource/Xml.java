/*
 * Copyright 2014 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.InputData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * XML data source compatible with the standard IDE approach.
 *
 * @author zarkonnen
 */
public class Xml implements FileDataSource {

    @Override
    public List<InputData> getData(final Map<String, String> config, final File relativeTo, final InputData vars) throws IOException {
        final ArrayList<InputData> data = new ArrayList<>();
        final File f = this.sourceFile(config, relativeTo, vars);
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            final NodeList rows = doc.getElementsByTagName("test");
            IntStream.range(0, rows.getLength()).forEach(i -> {
                final Node rowN = rows.item(i);
                final NamedNodeMap attributes = rowN.getAttributes();
                final LinkedHashMap<String, String> row = new LinkedHashMap<>();
                row.put(InputData.ROW_NUMBER, String.valueOf(i + 1));
                IntStream.range(0, attributes.getLength()).forEach(j ->
                        row.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue())
                );
                data.add(new InputData(row));
            });
            if (rows.getLength() > 0) {
                final int lastRowNumber = rows.getLength() - 1;
                final InputData lastRow = data.get(lastRowNumber).lastRow(true);
                data.remove(lastRowNumber);
                data.add(lastRow);
            }
        } catch (final Exception e) {
            throw new IOException("Unable to get data.", e);
        }
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
