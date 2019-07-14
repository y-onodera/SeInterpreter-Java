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
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sebuilder.interpreter.datasource.Utils.findFile;

/**
 * XML data source compatible with the standard IDE approach.
 *
 * @author zarkonnen
 */
public class Xml implements DataSource {
    @Override
    public List<TestData> getData(Map<String, String> config, File relativeTo, TestData vars) {
        ArrayList<TestData> data = Lists.newArrayList();
        File f = findFile(relativeTo, vars.bind(config.get("path")));
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            NodeList rows = doc.getElementsByTagName("test");
            for (int i = 0; i < rows.getLength(); i++) {
                Node rowN = rows.item(i);
                NamedNodeMap attributes = rowN.getAttributes();
                Map<String, String> row = new HashMap<String, String>();
                row.put(TestData.ROW_NUMBER, String.valueOf(i + 1));
                for (int j = 0; j < attributes.getLength(); j++) {
                    row.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
                }
                data.add(new TestData(row));
            }
            final int lastRowNumber = rows.getLength() - 1;
            TestData lastRow = data.get(lastRowNumber).lastRow(true);
            data.remove(lastRowNumber);
            data.add(lastRow);
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
