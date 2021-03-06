/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/*
 * @author max
 */
package com.intellij.lang.properties.parsing;

import com.intellij.lang.properties.psi.Property;
import com.intellij.lang.properties.psi.PropertyKeyIndex;
import com.intellij.lang.properties.psi.PropertyStub;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.lang.properties.psi.impl.PropertyStubImpl;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PropertyStubElementType extends IStubElementType<PropertyStub, Property> {
  public PropertyStubElementType() {
    super("PROPERTY", PropertiesElementTypes.LANG);
  }

  public Property createPsi(@NotNull final PropertyStub stub) {
    return new PropertyImpl(stub, this);
  }

  public PropertyStub createStub(@NotNull final Property psi, final StubElement parentStub) {
    return new PropertyStubImpl(parentStub, psi.getKey());
  }

  public String getExternalId() {
    return "properties.prop";
  }

  public void serialize(final PropertyStub stub, final StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getKey());
  }

  public PropertyStub deserialize(final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    final StringRef ref = dataStream.readName();
    return new PropertyStubImpl(parentStub, ref.getString());
  }

  public void indexStub(final PropertyStub stub, final IndexSink sink) {
    sink.occurrence(PropertyKeyIndex.KEY, PropertyImpl.unescape(stub.getKey()));
  }
}