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
//
//  MyWebLocationChangeAdapter.h
//  CarbonWeb
//
//  Created by Ed Voas on Tue Jan 28 2003.
//  Copyright (c) 2003 Apple Computer, Inc. All rights reserved.
//

#include <WebKit/WebKit.h>

@interface MyFrameLoadAdapter : NSObject
{
   	jobject			m_callback;
}

- initWithCallback: (jobject)callbackObject;

@end
