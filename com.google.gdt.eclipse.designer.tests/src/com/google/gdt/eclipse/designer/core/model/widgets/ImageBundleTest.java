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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;
import com.google.gdt.eclipse.designer.model.widgets.ImageInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMementoTransfer;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Test for {@link ImageBundleInfo}.
 * 
 * @author scheglov_ke
 */
public class ImageBundleTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // we create images, so dispose all
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noBundles() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    // no bundles
    {
      List<ImageBundleInfo> bundles = ImageBundleContainerInfo.getBundles(frame);
      assertThat(bundles).isEmpty();
    }
  }

  public void test_withBundle() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 16, 16);
    TestUtils.createImagePNG(m_testProject, "src/test/client/second.png", 16, 16);
    TestUtils.createImagePNG(m_testProject, "src/test/client/third.png", 16, 16);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "  AbstractImagePrototype second();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}",
        "  {com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
        "    {opaque} {field-initializer: m_myBundle} {/GWT.create(MyImageBundle.class)/}");
    // ImageBundleContainer_Info
    {
      ImageBundleContainerInfo container = ImageBundleContainerInfo.get(frame);
      assertEquals(
          "{com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
          container.toString());
      // presentation
      {
        IObjectPresentation presentation = container.getPresentation();
        assertEquals("(ImageBundle's)", presentation.getText());
        assertNotNull(presentation.getIcon());
      }
      // bundles
      {
        List<ImageBundleInfo> bundles = ImageBundleContainerInfo.getBundles(frame);
        assertThat(bundles).hasSize(1);
      }
    }
  }

  /**
   * Test for {@link ImageBundleContainerInfo#add(JavaInfo, String)}.
   */
  public void test_addBundle() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    // add ImageBundle
    ImageBundleContainerInfo.add(frame, "test.client.MyImageBundle");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle myImageBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}",
        "  {com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
        "    {opaque} {field-initializer: myImageBundle} {/GWT.create(MyImageBundle.class)/}");
  }

  /**
   * Test for {@link ImageBundlePrototypeDescription}'s.
   */
  public void test_imagePrototypes() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    TestUtils.createImagePNG(m_testProject, "src/test/client/second.png", 2, 2);
    TestUtils.createImagePNG(m_testProject, "src/test/client/deep/third.png", 3, 3);
    TestUtils.createImagePNG(m_testProject, "src/test/client/big.png", 64, 40);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "  @Resource('second.png')",
            "  AbstractImagePrototype second();",
            "  @Resource('test/client/deep/third.png')",
            "  AbstractImagePrototype third();",
            "  AbstractImagePrototype big();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    // check prototypes
    {
      ImageBundleInfo bundle = ImageBundleContainerInfo.getBundles(frame).get(0);
      List<ImageBundlePrototypeDescription> prototypes = bundle.getPrototypes();
      assertThat(prototypes).hasSize(4);
      for (ImageBundlePrototypeDescription prototype : prototypes) {
        assertSame(bundle, prototype.getBundle());
        // first(), no @Resource
        if (prototype.getMethod().getName().equals("first")) {
          // image
          {
            Image image = prototype.getImage();
            assertNotNull(image);
            assertThat(image.getBounds().width).isEqualTo(1);
            assertThat(image.getBounds().height).isEqualTo(1);
          }
          // icon
          {
            Image icon = prototype.getIcon();
            assertNotNull(icon);
            assertThat(icon.getBounds().width).isEqualTo(16);
            assertThat(icon.getBounds().height).isEqualTo(16);
          }
        }
        // second(), has @Resource in package
        if (prototype.getMethod().getName().equals("second")) {
          // image
          {
            Image image = prototype.getImage();
            assertNotNull(image);
            assertThat(image.getBounds().width).isEqualTo(2);
            assertThat(image.getBounds().height).isEqualTo(2);
          }
          // icon
          {
            Image icon = prototype.getIcon();
            assertNotNull(icon);
            assertThat(icon.getBounds().width).isEqualTo(16);
            assertThat(icon.getBounds().height).isEqualTo(16);
          }
        }
        // third(), has @Resource with full path
        if (prototype.getMethod().getName().equals("third")) {
          // image
          {
            Image image = prototype.getImage();
            assertNotNull(image);
            assertThat(image.getBounds().width).isEqualTo(3);
            assertThat(image.getBounds().height).isEqualTo(3);
          }
          // icon
          {
            Image icon = prototype.getIcon();
            assertNotNull(icon);
            assertThat(icon.getBounds().width).isEqualTo(16);
            assertThat(icon.getBounds().height).isEqualTo(16);
          }
        }
        // big(), has size 64x40
        if (prototype.getMethod().getName().equals("big")) {
          // image
          {
            Image image = prototype.getImage();
            assertNotNull(image);
            assertThat(image.getBounds().width).isEqualTo(64);
            assertThat(image.getBounds().height).isEqualTo(40);
          }
          // icon
          {
            Image icon = prototype.getIcon();
            assertNotNull(icon);
            assertThat(icon.getBounds().width).isEqualTo(51);
            assertThat(icon.getBounds().height).isEqualTo(32);
          }
        }
      }
    }
  }

  /**
   * Test for {@link ImageBundlePrototypeDescription#isRepresentedBy(Expression)}.
   */
  public void test_ImagePrototype_isRepresentedBy() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    TestUtils.createImagePNG(m_testProject, "src/test/client/second.png", 2, 2);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "  AbstractImagePrototype second();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(m_myBundle.first().createImage());",
            "    rootPanel.add(m_myBundle.second().createImage());",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    // prepare Expression's
    List<WidgetInfo> images = frame.getChildrenWidgets();
    Expression firstExpression =
        ((MethodInvocation) images.get(0).getCreationSupport().getNode()).getExpression();
    Expression secondExpression =
        ((MethodInvocation) images.get(1).getCreationSupport().getNode()).getExpression();
    // prepare prototypes
    List<ImageBundlePrototypeDescription> prototypes;
    {
      ImageBundleInfo bundle = ImageBundleContainerInfo.getBundles(frame).get(0);
      prototypes = bundle.getPrototypes();
      assertThat(prototypes).hasSize(2);
    }
    // check prototypes
    assertFalse(prototypes.get(0).isRepresentedBy(null));
    assertTrue(prototypes.get(0).isRepresentedBy(firstExpression));
    assertFalse(prototypes.get(0).isRepresentedBy(secondExpression));
    assertTrue(prototypes.get(1).isRepresentedBy(secondExpression));
    assertFalse(prototypes.get(1).isRepresentedBy(firstExpression));
  }

  /**
   * Test for {@link ImageBundlePrototypeDescription#createImageWidget()}.
   */
  public void test_createImage_CREATE() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 10, 20);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    // prepare prototype
    ImageBundlePrototypeDescription prototype;
    {
      ImageBundleInfo bundle = ImageBundleContainerInfo.getBundles(frame).get(0);
      List<ImageBundlePrototypeDescription> prototypes = bundle.getPrototypes();
      prototype = prototypes.get(0);
    }
    // create new Image
    ImageInfo newImage = prototype.createImageWidget();
    newImage.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    // check live properties
    {
      assertNotNull(newImage.getImage());
      assertThat(newImage.getPreferredSize().width).isEqualTo(10);
      assertThat(newImage.getPreferredSize().height).isEqualTo(20);
      assertThat(newImage.shouldSetReasonableSize()).isFalse();
    }
    // do add
    frame.command_CREATE2(newImage, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = m_myBundle.first().createImage();",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
    // permissions
    {
      CreationSupport creationSupport = newImage.getCreationSupport();
      assertTrue(creationSupport.canReorder());
      assertTrue(creationSupport.canReparent());
      assertTrue(creationSupport.canDelete());
    }
    // assert that "image" is bound to AST
    {
      ASTNode node = getNode("image = ");
      assertTrue(newImage.isRepresentedBy(node));
    }
  }

  /**
   * Test for parsing <code>AbstractImagePrototype#createImage()</code>.
   */
  public void test_createImage_PARSE() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Image image = m_myBundle.first().createImage();",
            "      rootPanel.add(image);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(image)/}",
        "  {opaque} {local-unique: image} {/m_myBundle.first().createImage()/ /rootPanel.add(image)/}",
        "  {com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
        "    {opaque} {field-initializer: m_myBundle} {/GWT.create(MyImageBundle.class)/ /m_myBundle.first()/}");
  }

  public void test_createImage_PERMISSIONS() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Image image = m_myBundle.first().createImage();",
            "      rootPanel.add(image);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ImageInfo image = (ImageInfo) frame.getChildrenWidgets().get(0);
    //
    CreationSupport creationSupport = image.getCreationSupport();
    // permissions
    assertTrue(creationSupport.canReorder());
    assertTrue(creationSupport.canReparent());
    // delete
    assertTrue(creationSupport.canDelete());
    image.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }

  /**
   * <code>Image</code> has method <code>int getWidth()</code>, so this makes <code>width</code>
   * property with <code>setWidth(String)</code> not existing, set we need explicit specification
   * for <code>setWidth(String)</code> execution.
   */
  public void test_createImage_setWidth() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Image image = m_myBundle.first().createImage();",
            "      rootPanel.add(image);",
            "      image.setWidth('150px');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ImageInfo image = (ImageInfo) frame.getChildrenWidgets().get(0);
    //
    assertEquals(150, image.getBounds().width);
  }

  /**
   * {@link ImageBundlePrototypeDescription} should be contributed to palette.
   */
  public void test_contributeToPalette() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertNoErrors(frame);
    // prepare prototype
    ImageBundlePrototypeDescription prototype;
    {
      ImageBundleInfo bundle = ImageBundleContainerInfo.getBundles(frame).get(0);
      List<ImageBundlePrototypeDescription> prototypes = bundle.getPrototypes();
      prototype = prototypes.get(0);
    }
    // prepare category/entries
    CategoryInfo category = new CategoryInfo();
    category.setId("com.google.gdt.eclipse.designer.ImageBundle");
    List<EntryInfo> entries = Lists.newArrayList();
    // send palette broadcast
    PaletteEventListener listener = frame.getBroadcast(PaletteEventListener.class);
    listener.entries(category, entries);
    // we should have exactly one entry
    ToolEntryInfo toolEntry;
    {
      assertEquals(1, entries.size());
      toolEntry = (ToolEntryInfo) entries.get(0);
    }
    // check Entry presentation
    {
      assertEquals(ObjectUtils.identityToString(prototype), toolEntry.getId());
      assertThat(toolEntry.getName()).isEqualTo("first");
      assertThat(toolEntry.getDescription()).contains("first()");
      assertSame(prototype.getIcon(), toolEntry.getIcon());
    }
    // use this entry to create new Image widget
    WidgetInfo newImage;
    {
      toolEntry.initialize(null, frame);
      CreationTool creationTool = (CreationTool) toolEntry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newImage = (WidgetInfo) creationFactory.getNewObject();
    }
    //
    frame.command_CREATE2(newImage, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = m_myBundle.first().createImage();",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ImageBundle as parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * In general, when we see <code>MyImageBundle</code> parameter we evaluate it as "null". However
   * in GWT we known which value it will have, so can evaluate it better.
   */
  public void test_withBundle_asConstructorParameter() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 16, 16);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test(MyImageBundle images) {",
            "    Image image = images.first().createImage();",
            "    initWidget(image);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(image)/}",
        "  {opaque} {local-unique: image} {/images.first().createImage()/ /initWidget(image)/}");
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private static byte[] m_clipboardTreeBytes;

  public void test_clipboard_0() throws Exception {
    prepare_clipboard_MyImageBundle();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree0 = new Tree(m_myBundle);",
            "      rootPanel.add(tree0);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree0)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree0} {/new Tree(m_myBundle)/ /rootPanel.add(tree0)/}",
        "  {com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
        "    {opaque} {field-initializer: m_myBundle} {/GWT.create(MyImageBundle.class)/ /new Tree(m_myBundle)/}");
    frame.refresh();
    // copy Tree into bytes
    {
      WidgetInfo tree = frame.getChildrenWidgets().get(0);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(tree);
      m_clipboardTreeBytes = JavaInfoMementoTransfer.convertObjectToBytes(memento);
    }
  }

  public void test_clipboard_existingInstance() throws Exception {
    prepare_clipboard_MyImageBundle();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle someBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}",
        "  {com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo}",
        "    {opaque} {field-initializer: someBundle} {/GWT.create(MyImageBundle.class)/}");
    frame.refresh();
    // paste Tree
    addTreeFromMemento(frame);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle someBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree(someBundle);",
        "      rootPanel.add(tree);",
        "    }",
        "  }",
        "}");
  }

  public void test_clipboard_addNewInstance() throws Exception {
    prepare_clipboard_MyImageBundle();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // paste Tree
    addTreeFromMemento(frame);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyImageBundle myImageBundle = GWT.create(MyImageBundle.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree(myImageBundle);",
        "      rootPanel.add(tree);",
        "    }",
        "  }",
        "}");
  }

  public void test_clipboard_noSuchImageBundle() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // paste Tree
    try {
      addTreeFromMemento(frame);
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.NO_SUCH_IMAGE_BUNDLE, de.getCode());
    }
  }

  private void prepare_clipboard_MyImageBundle() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 16, 16);
    setFileContentSrc(
        "test/client/MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends TreeImages {",
            "  @Resource('first.png')",
            "  AbstractImagePrototype treeOpen();",
            "}"));
    waitForAutoBuild();
  }

  private void addTreeFromMemento(final RootPanelInfo frame) throws Exception {
    final JavaInfoMemento memento =
        (JavaInfoMemento) JavaInfoMementoTransfer.convertBytesToObject(m_clipboardTreeBytes);
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newTree = (WidgetInfo) memento.create(frame);
        frame.command_CREATE2(newTree, null);
        memento.apply();
      }
    });
  }
}