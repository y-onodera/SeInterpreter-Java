package com.sebuilder.interpreter;

import java.io.IOException;
import java.util.ArrayList;

public interface DataSourceWriter {
    void write(ArrayList<InputData> saveContents) throws IOException;
}
