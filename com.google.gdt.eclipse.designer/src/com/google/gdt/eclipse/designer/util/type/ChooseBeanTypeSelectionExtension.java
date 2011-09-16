/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.util.type;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoImageProvider;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TypeSelectionExtension} that allows user dynamically select one of the
 * provided contributors and so filter types in type selection dialog.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.beanSelection
 */
public class ChooseBeanTypeSelectionExtension extends TypeSelectionExtension {
  private final IPackageFragment m_packageFragment;
  private final List<IChooseBeanContributor> m_contributors;
  private final ChooseBeanDynamicProvider m_dynamicProvider = new ChooseBeanDynamicProvider();
  private IChooseBeanContributor m_selectedContributor;
  private Button[] m_contributorButtons;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseBeanTypeSelectionExtension(IPackageFragment packageFragment,
      IJavaSearchScope searchScope,
      IChooseBeanContributor[] contributors) {
    m_packageFragment = packageFragment;
    m_contributors = Arrays.asList(contributors);
    if (m_contributors.size() > 0) {
      selectContributor(m_contributors.get(0));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TypeSelectionExtension
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control createContentArea(Composite parent) {
    // type selection area
    Composite area = new Composite(parent, SWT.NONE);
    GridDataFactory.create(area).grab().fill();
    GridLayoutFactory.create(area).noMargins();
    // Styles group
    if (m_contributors.size() > 0) {
      int numColumns = 1 + m_contributors.size() * 2;
      Composite contributorComposite = new Composite(area, SWT.NONE);
      GridDataFactory.create(contributorComposite).grabH().fillH();
      GridLayoutFactory.create(contributorComposite).columns(numColumns).noMargins();
      //
      {
        Label contributorLabel = new Label(contributorComposite, SWT.NONE);
        GridDataFactory.create(contributorLabel).spanH(numColumns);
        contributorLabel.setText("Styles:");
      }
      // add spacer (for indentation)
      new Label(contributorComposite, SWT.NONE);
      // add image and button for each contributor
      final String contributorKey = "contributor";
      m_contributorButtons = new Button[m_contributors.size()];
      for (int i = 0; i < m_contributors.size(); i++) {
        IChooseBeanContributor contributor = m_contributors.get(i);
        // add image label for contributor
        {
          Label imageLabel = new Label(contributorComposite, SWT.NONE);
          ImageDescriptor imageDescriptor = contributor.getImage();
          final Image image = imageDescriptor == null ? null : imageDescriptor.createImage();
          if (image != null) {
            imageLabel.setImage(image);
            imageLabel.addDisposeListener(new DisposeListener() {
              public void widgetDisposed(DisposeEvent e) {
                image.dispose();
              }
            });
          }
        }
        // add button
        Button button = new Button(contributorComposite, SWT.RADIO);
        m_contributorButtons[i] = button;
        button.setText(contributor.getName());
        button.setData(contributorKey, contributor);
        button.addSelectionListener(new SelectionListener() {
          public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.getSource();
            if (button.getSelection()) {
              IChooseBeanContributor contributor =
                  (IChooseBeanContributor) button.getData(contributorKey);
              selectContributor(contributor);
            }
          }

          public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
          }
        });
      }
      // select button for selected (first) contributor
      if (m_selectedContributor != null) {
        int index = m_contributors.indexOf(m_selectedContributor);
        m_contributorButtons[index].setSelection(true);
      }
    }
    // finished
    return area;
  }

  @Override
  public ITypeInfoFilterExtension getFilterExtension() {
    return m_dynamicProvider;
  }

  @Override
  public ITypeInfoImageProvider getImageProvider() {
    return m_dynamicProvider;
  }

  private void selectContributor(IChooseBeanContributor contrib) {
    m_dynamicProvider.clear();
    m_selectedContributor = contrib;
    if (m_selectedContributor != null && getTypeSelectionComponent() != null) {
      getTypeSelectionComponent().triggerSearch();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dynamic provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private class ChooseBeanDynamicProvider
      implements
        ITypeInfoFilterExtension,
        ITypeInfoImageProvider {
    private final Map<IChooseBeanContributor, ITypeInfoFilterExtension> m_selectedContributorToFilterMap =
        new HashMap<IChooseBeanContributor, ITypeInfoFilterExtension>();
    private final Map<IChooseBeanContributor, ImageDescriptor> m_selectedContributorToImageMap =
        new HashMap<IChooseBeanContributor, ImageDescriptor>();
    private ITypeInfoFilterExtension m_currentContributorFilter;
    private ImageDescriptor m_currentContributorImage;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void clear() {
      m_currentContributorFilter = null;
      m_currentContributorImage = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ITypeInfoFilterExtension
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean select(ITypeInfoRequestor typeInfoRequestor) {
      if (m_currentContributorFilter == null) {
        if (m_selectedContributor != null) {
          if (!m_selectedContributorToFilterMap.containsKey(m_selectedContributor)) {
            ITypeInfoFilterExtension filter =
                m_selectedContributor.getFilter(m_packageFragment, new NullProgressMonitor());
            m_selectedContributorToFilterMap.put(m_selectedContributor, filter);
          }
          m_currentContributorFilter = m_selectedContributorToFilterMap.get(m_selectedContributor);
        }
      }
      if (m_currentContributorFilter != null) {
        return m_currentContributorFilter.select(typeInfoRequestor);
      }
      return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ITypeInfoImageProvider
    //
    ////////////////////////////////////////////////////////////////////////////
    public ImageDescriptor getImageDescriptor(ITypeInfoRequestor typeInfoRequestor) {
      if (m_currentContributorImage == null) {
        if (m_selectedContributor != null) {
          if (!m_selectedContributorToImageMap.containsKey(m_selectedContributor)) {
            ImageDescriptor imageDescriptor = m_selectedContributor.getImage();
            m_selectedContributorToImageMap.put(m_selectedContributor, imageDescriptor);
          }
          m_currentContributorImage = m_selectedContributorToImageMap.get(m_selectedContributor);
        }
      }
      return m_currentContributorImage;
    }
  }
}
