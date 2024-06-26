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


package com.sebuilder.interpreter;

import java.io.File;

public final class Utils {
    private Utils() {
    }

    public static File findFile(final File relativeTo, final String path) {
        final File precedence = new File(Context.getDataSourceDirectory(), path);
        if (precedence.exists()) {
            return precedence;
        }
        if (relativeTo != null) {
            final File f = new File(relativeTo, path);
            if (f.exists()) {
                return f;
            }
        }
        return new File(path);
    }
}
