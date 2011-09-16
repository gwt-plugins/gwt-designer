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
package com.google.gdt.eclipse.designer.css;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.css.editors.CssConfiguration;
import org.eclipse.wb.internal.css.editors.CssPartitionScanner;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.IPage;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link IPage} with list of related styles.
 * 
 * @author scheglov_ke
 * @coverage gwt.css
 */
public class StyleListPage extends ScrolledComposite {
  private final PixelConverter m_pixelConverter;
  private final int m_pixelCharOne;
  private final Composite m_contentComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleListPage(Composite parent, int style) {
    super(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    {
      m_pixelConverter = new PixelConverter(this);
      m_pixelCharOne = m_pixelConverter.convertWidthInCharsToPixels(1);
    }
    useMouseWheelForVerticalScrolling();
    // configure scrolled
    setAlwaysShowScrollBars(false);
    setExpandHorizontal(true);
    // use this Composite as container for all parts
    m_contentComposite = new Composite(this, SWT.NONE);
    GridLayoutFactory.create(m_contentComposite).noMargins().noSpacing();
    setContent(m_contentComposite);
    //
    createRuleViewer(m_contentComposite, "Matched CSS Rules", "body {\n"
        + "\tdisplay: block;\n"
        + "\tdisplay: block;\n"
        + "\tdisplay: block;\n"
        + "\t/*display: block;*/\n"
        + "\tmargin: 1em 40px;\n"
        + "}");
    createRuleViewer(m_contentComposite, "Inherited from span#search", "div {\n"
        + "\tdisplay: inline;\n"
        + "\tdisplay: inline;\n"
        + "\tdisplay: inline;\n"
        + "\tmargin: 1em 40px;\n"
        + "}");
    createRuleViewer(m_contentComposite, "Inherited from span#main", "div {\n"
        + "\tdisplay: aaaaaaaa;\n"
        + "\tdisplay: aaaaaaaa;\n"
        + "\tdisplay: aaaaaaaa;\n"
        + "\tdisplay: aaaaaaaa;\n"
        + "\tmargin: 1em 40px;\n"
        + "}");
    createRuleViewer(m_contentComposite, "Inherited from body", "div {\n"
        + "\tdisplay: bbbbbbbbbbb;\n"
        + "\tdisplay: bbbbbbbbbbb;\n"
        + "\tdisplay: bbbbbbbbbbb;\n"
        + "\tmargin: 1em 40px;\n"
        + "}");
    createRuleViewer(m_contentComposite, "Inherited from html", "div {\n"
        + "\tdisplay: ccccccccccccc;\n"
        + "\tdisplay: ccccccccccccc;\n"
        + "\tdisplay: ccccccccccccc;\n"
        + "\tdisplay: ccccccccccccc;\n"
        + "\tdisplay: ccccccccccccc;\n"
        + "\tmargin: 1em 40px;\n"
        + "}");
    // update content size now
    updateContentSize();
    //
    addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        ScrollBar vBar = getVerticalBar();
        vBar.setIncrement(m_pixelCharOne);
        vBar.setPageIncrement(getSize().y);
      }
    });
  }

  /**
   * Updates size of {@link #m_contentComposite} after any change in its children.
   */
  private void updateContentSize() {
    Point contentSize = m_contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    m_contentComposite.setSize(contentSize);
  }

  private void createRuleViewer(Composite parent, String title, String rule) {
    createRuleTitle(parent, title);
    //
    final SourceViewer viewer = createCssViewer(parent, SWT.NONE);
    final StyledText viewerControl = viewer.getTextWidget();
    GridDataFactory.create(viewerControl).grabH().fill();
    viewer.getDocument().set(rule);
    viewer.addTextListener(new ITextListener() {
      public void textChanged(TextEvent event) {
        updateContentSize();
      }
    });
    // setup navigation
    setupKeyboardNavigation(viewerControl);
  }

  private void createRuleTitle(Composite parent, String title) {
    {
      Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
      GridDataFactory.create(separator).grabH().fillH();
    }
    {
      Composite titleComposite = new Composite(parent, SWT.NONE);
      GridDataFactory.create(titleComposite).grabH().fillH();
      GridLayoutFactory.create(titleComposite).noMargins();
      {
        Label titleLabel = new Label(titleComposite, SWT.NONE);
        titleLabel.setText(title);
        titleLabel.setFont(SwtResourceManager.getFont("Small", 8, SWT.BOLD));
      }
    }
    {
      Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
      GridDataFactory.create(separator).grabH().fillH();
    }
  }

  private static SourceViewer createCssViewer(Composite parent, int style) {
    SourceViewer viewer = new SourceViewer(parent, null, style);
    // set document
    {
      Document document = new Document();
      viewer.setDocument(document);
      CssPartitionScanner.configurePartitions(document);
    }
    // configure viewer
    viewer.configure(new CssConfiguration());
    return viewer;
  }

  /**
   * Configures SWT to use mouse wheel for vertical scrolling on this {@link ScrolledComposite}.
   */
  private void useMouseWheelForVerticalScrolling() {
    final Display display = getDisplay();
    final Listener wheelListener = new Listener() {
      public void handleEvent(Event event) {
        Control cursorControl = display.getCursorControl();
        boolean isChild = UiUtils.isChildOf(StyleListPage.this, cursorControl);
        if (isChild) {
          OSSupport.get().scroll(StyleListPage.this, event.count);
        }
      }
    };
    addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        display.removeFilter(SWT.MouseWheel, wheelListener);
      }
    });
    display.addFilter(SWT.MouseWheel, wheelListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard navigation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds support for scrolling up/down between sibling {@link StyledText}s and keeping caret
   * visible by scrolling {@link ScrolledComposite}.
   */
  private void setupKeyboardNavigation(final StyledText text) {
    final AtomicLong lastCaretTime = new AtomicLong();
    text.addCaretListener(new CaretListener() {
      public void caretMoved(CaretEvent event) {
        lastCaretTime.set(event.time);
        ensureCaretVisible(text);
      }
    });
    text.addListener(SWT.KeyDown, new Listener() {
      public void handleEvent(Event event) {
        // if StyledText already handle key and moved caret, then not first/last line
        if (lastCaretTime.get() == event.time) {
          return;
        }
        // prepare current line
        int caretOffset = text.getCaretOffset();
        int line = text.getLineAtOffset(caretOffset);
        // up
        if (event.keyCode == SWT.ARROW_UP && line == 0) {
          StyledText prevText = getStyledTextSibling(text, false);
          if (prevText != null) {
            int prevLineCount = prevText.getLineCount();
            if (prevLineCount != 0) {
              int offset = prevText.getOffsetAtLine(prevLineCount - 1);
              prevText.setCaretOffset(offset);
            } else {
              prevText.setCaretOffset(0);
            }
            ensureCaretVisible(prevText);
            prevText.setFocus();
          }
        }
        // down
        if (event.keyCode == SWT.ARROW_DOWN && line == text.getLineCount() - 1) {
          StyledText nextText = getStyledTextSibling(text, true);
          if (nextText != null) {
            nextText.setCaretOffset(0);
            ensureCaretVisible(nextText);
            nextText.setFocus();
          }
        }
      }
    });
  }

  /**
   * @return the {@link StyledText} before/after given on same parent, may be <code>null</code>.
   */
  private static StyledText getStyledTextSibling(StyledText text, boolean next) {
    List<Control> allSiblings = ImmutableList.copyOf(text.getParent().getChildren());
    List<StyledText> siblings = GenericsUtils.select(allSiblings, StyledText.class);
    if (next) {
      return GenericsUtils.getNextOrNull(siblings, text);
    } else {
      return GenericsUtils.getPrevOrNull(siblings, text);
    }
  }

  /**
   * Ensures that {@link ScrolledComposite} origin is adjusted so that caret at given offset in
   * {@link StyledText} is visible to user.
   */
  private void ensureCaretVisible(StyledText text) {
    int origin = getOrigin().y;
    int height = getClientArea().height;
    // prepare position in ScrolledComposite
    int caretOffset = text.getCaretOffset();
    int y = text.getBounds().y + text.getLocationAtOffset(caretOffset).y;
    int lineHeight = text.getLineHeight(caretOffset);
    // above viewport
    if (y < origin) {
      setOrigin(0, y);
    }
    // below viewport
    if (y + lineHeight > origin + height) {
      setOrigin(0, y - height + lineHeight);
    }
    // almost top
    if (y < 3 * lineHeight) {
      setOrigin(0, 0);
    }
    // almost bottom
    {
      int fullHeight = getContent().getSize().y;
      if (y + 3 * lineHeight > fullHeight) {
        setOrigin(0, fullHeight);
      }
    }
  }
}
