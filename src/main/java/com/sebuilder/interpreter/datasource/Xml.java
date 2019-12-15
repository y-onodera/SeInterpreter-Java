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

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.InputData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XML data source compatible with the standard IDE approach.
 *
 * @author zarkonnen
 */
public class Xml implements FileDataSource {

    @Override
    public List<InputData> getData(Map<String, String> config, File relativeTo, InputData vars) {
        ArrayList<InputData> data = Lists.newArrayList();
        File f = this.sourceFile(config, relativeTo, vars);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            NodeList rows = doc.getElementsByTagName("test");
            for (int i = 0; i < rows.getLength(); i++) {
                Node rowN = rows.item(i);
                NamedNodeMap attributes = rowN.getAttributes();
                LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
                row.put(InputData.ROW_NUMBER, String.valueOf(i + 1));
                for (int j = 0; j < attributes.getLength(); j++) {
                    row.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
                }
                data.add(new InputData(row));
            }
            if (rows.getLength() > 0) {
                final int lastRowNumber = rows.getLength() - 1;
                InputData lastRow = data.get(lastRowNumber).lastRow(true);
                data.remove(lastRowNumber);
                data.add(lastRow);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get data.", e);
        }
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
