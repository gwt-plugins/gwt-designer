/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
//
//  NSBrowserShell.h
//  wbp-gwt-cocoa
//
//  Created by Alexander Mitin on 5/26/09.
//  Copyright 2009 Instantiations, Inc. All rights reserved.
//

@interface NSBrowserShell : NSWindow {
	WebView* m_webView;
}

- (id)initWithCallback:(jobject)callbackObject;
- (void) setUrl:(NSString*)url;
- (WebView*) webView;

@end
