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
#ifndef TCarbonEvent_H_
#define TCarbonEvent_H_

#include <Carbon/Carbon.h>

class TCarbonEvent
{
public:
	// Construction/Destruction
	TCarbonEvent(
					UInt32				inClass,
					UInt32				inKind );
	TCarbonEvent(
					EventRef			inEvent );
	virtual ~TCarbonEvent();
	
	UInt32		GetClass() const;
	UInt32		GetKind() const;
	
	// Time
	void		SetTime( EventTime inTime );
	EventTime	GetTime() const;
	
	// Retention
	void		Retain();
	void		Release();
	
	// Accessors
	operator		EventRef&()
						{ return fEvent; };
	EventRef		GetEventRef()
						{ return fEvent; }
	
	// Posting
	OSStatus 	PostToQueue(
					EventQueueRef		inQueue,
					EventPriority		inPriority = kEventPriorityStandard );

	// Parameters
	OSStatus	SetParameter(
					EventParamName		inName,
					EventParamType		inType,
					UInt32				inSize,
					const void*			inData );
	OSStatus	GetParameter(
					EventParamName		inName,
					EventParamType		inType,
					UInt32				inBufferSize,
					void*				outData );

	OSStatus	GetParameterType(
					EventParamName		inName,
					EventParamType*		outType );
	OSStatus	GetParameterSize(
					EventParamName		inName,
					UInt32*				outSize );

	// Simple parameters
	OSStatus	SetParameter(
					EventParamName		inName,
					Boolean				inValue );
	OSStatus	GetParameter(
					EventParamName		inName,
					Boolean*			outValue );

	OSStatus	SetParameter(
					EventParamName		inName,
					bool				inValue );
	OSStatus	GetParameter(
					EventParamName		inName,
					bool*				outValue );

	OSStatus	SetParameter(
					EventParamName		inName,
					Point				inPt );
	OSStatus	GetParameter(
					EventParamName		inName,
					Point*				outPt );

	OSStatus	SetParameter(
					EventParamName		inName,
					const HIPoint&		inPt );

	OSStatus	GetParameter(
					EventParamName		inName,
					HIPoint*			outPt );

	OSStatus	SetParameter(
					EventParamName		inName,
					const Rect&			inRect );
	OSStatus	GetParameter(
					EventParamName		inName,
					Rect*				outRect );

	OSStatus	SetParameter(
					EventParamName		inName,
					const HIRect&		inRect );
	OSStatus	GetParameter(
					EventParamName		inName,
					HIRect*				outRect );

	OSStatus	SetParameter(
					EventParamName		inName,
					const HISize&		inSize );
	OSStatus	GetParameter(
					EventParamName		inName,
					HISize*				outSize );

	OSStatus	SetParameter(
					EventParamName		inName,
					RgnHandle			inRegion );
	OSStatus	GetParameter(
					EventParamName		inName,
					RgnHandle*			outRegion );

	OSStatus	SetParameter(
					EventParamName		inName,
					WindowRef			inWindow );
	OSStatus	GetParameter(
					EventParamName		inName,
					WindowRef*			outWindow );

	OSStatus	SetParameter(
					EventParamName		inName,
					ControlRef			inControl );
	OSStatus	GetParameter(
					EventParamName		inName,
					ControlRef* outControl );

	OSStatus	SetParameter(
					EventParamName		inName,
					MenuRef				inMenu );
	OSStatus	GetParameter(
					EventParamName		inName,
					MenuRef*			outMenu );

	OSStatus	SetParameter(
					EventParamName		inName,
					DragRef				inDrag );
	OSStatus	GetParameter(
					EventParamName		inName,
					DragRef*			outDrag );

	OSStatus	SetParameter(
					EventParamName		inName,
					UInt32				inValue );
	OSStatus	GetParameter(
					EventParamName		inName,
					UInt32*				outValue );
	
	OSStatus	SetParameter(
					EventParamName		inName,
					const HICommand&	inValue );
	OSStatus	GetParameter(
					EventParamName		inName,
					HICommand*			outValue );

	// Template parameters
	template <class T> OSStatus SetParameter(
		EventParamName	inName,
		EventParamType	inType,
		const T&		inValue )
	{
		return SetParameter( inName, inType, sizeof( T ), &inValue );
	}
			
	template <class T> OSStatus GetParameter(
		EventParamName	inName,
		EventParamType	inType,
		T*				outValue )
	{
		return GetParameter( inName, inType, sizeof( T ), outValue );
	}
	
private:
	EventRef	fEvent;
};

#endif // TCarbonEvent_H_
