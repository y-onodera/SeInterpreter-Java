package com.sebuilder.interpreter;

import java.io.IOException;
import java.util.ArrayList;

public interface DataSourceWriter {
    void writer(ArrayList<TestData> saveContents) throws IOException;
}
