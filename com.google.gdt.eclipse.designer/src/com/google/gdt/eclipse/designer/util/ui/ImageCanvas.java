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
package com.google.gdt.eclipse.designer.util.ui;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Canvas for displaying {@link Image}.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.ui
 */
public final class ImageCanvas extends Canvas {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageCanvas(Composite parent, int style, final Image image) {
    super(parent, style);
    addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle clientArea = getClientArea();
        // draw image
        DrawUtils.drawScaledImage(gc, image, clientArea);
        // draw size message
        {
          String message = image.getBounds().width + "x" + image.getBounds().height;
          Point extent = gc.textExtent(message);
          int x = (clientArea.width - extent.x) / 2;
          int y = clientArea.height - extent.y - 5;
          gc.drawText(message, x, y);
        }
      }
    });
  }
}
