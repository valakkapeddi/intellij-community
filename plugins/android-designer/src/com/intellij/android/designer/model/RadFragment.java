/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.android.designer.model;

import com.intellij.android.designer.propertyTable.FragmentProperty;
import com.intellij.android.designer.propertyTable.IdProperty;
import com.intellij.android.designer.propertyTable.editors.FragmentDialog;
import com.intellij.android.designer.propertyTable.editors.ResourceEditor;
import com.intellij.designer.propertyTable.Property;
import com.intellij.designer.propertyTable.editors.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.android.dom.attrs.AttributeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Lobas
 */
public class RadFragment extends RadViewComponent implements IConfigurableComponent {
  private static final Property NAME_PROPERTY =
    new FragmentProperty("name", new ResourceEditor(null, Collections.<AttributeFormat>emptySet(), null) {
      @Override
      protected void showDialog() {
        Module module = myRootComponent.getClientProperty(ModelParser.MODULE_KEY);
        FragmentDialog dialog = new FragmentDialog(module);
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
          setValue(dialog.getFragmentName());
        }
      }
    });
  private static final Property TAG_PROPERTY = new FragmentProperty("tag", new TextEditor());
  private static final String NAME_KEY = "fragment.name";

  @Override
  public String getCreationXml() {
    return "<fragment android:layout_width=\"wrap_content\"\n" +
           "android:layout_height=\"wrap_content\"\n" +
           "android:name=\"" +
           extractClientProperty(NAME_KEY) +
           "\"/>";
  }

  @Override
  public void configure(Module module) throws Exception {
    FragmentDialog dialog = new FragmentDialog(module);
    dialog.show();

    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      setClientProperty(NAME_KEY, dialog.getFragmentName());
    }
    else {
      throw new Exception();
    }
  }

  @Override
  public void setProperties(List<Property> properties) {
    if (!properties.isEmpty()) {
      properties = new ArrayList<Property>(properties);
      properties.add(NAME_PROPERTY);
      properties.add(IdProperty.INSTANCE);
      properties.add(TAG_PROPERTY);
    }
    super.setProperties(properties);
  }
}