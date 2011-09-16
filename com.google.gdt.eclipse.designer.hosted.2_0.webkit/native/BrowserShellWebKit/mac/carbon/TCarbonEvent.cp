#include "TCarbonEvent.h"

//-----------------------------------------------------------------------------------
//	TCarbonEvent constructor
//-----------------------------------------------------------------------------------
//
TCarbonEvent::TCarbonEvent(
	UInt32				inClass,
	UInt32				inKind )
{
	CreateEvent( NULL, inClass, inKind, GetCurrentEventTime(), 0, &fEvent );
}

//-----------------------------------------------------------------------------------
//	TCarbonEvent constructor
//-----------------------------------------------------------------------------------
//
TCarbonEvent::TCarbonEvent(
	EventRef			inEvent )
{
	fEvent = inEvent;
	RetainEvent( fEvent );
}

//-----------------------------------------------------------------------------------
//	TCarbonEvent destructor
//-----------------------------------------------------------------------------------
//
TCarbonEvent::~TCarbonEvent()
{
	ReleaseEvent( fEvent );
}

//-----------------------------------------------------------------------------------
//	GetClass
//-----------------------------------------------------------------------------------
//
UInt32 TCarbonEvent::GetClass() const
{
	return GetEventClass( fEvent );
}

//-----------------------------------------------------------------------------------
//	GetKind
//-----------------------------------------------------------------------------------
//
UInt32 TCarbonEvent::GetKind() const
{
	return GetEventKind( fEvent );
}

//-----------------------------------------------------------------------------------
//	SetTime
//-----------------------------------------------------------------------------------
//
void TCarbonEvent::SetTime(
	EventTime			inTime )
{
	SetEventTime( fEvent, inTime );
}

//-----------------------------------------------------------------------------------
//	GetTime
//-----------------------------------------------------------------------------------
//
EventTime TCarbonEvent::GetTime() const
{
	return GetEventTime( fEvent );
}

//-----------------------------------------------------------------------------------
//	Retain
//-----------------------------------------------------------------------------------
//
void TCarbonEvent::Retain()
{
	RetainEvent( fEvent );
}

//-----------------------------------------------------------------------------------
//	Release
//-----------------------------------------------------------------------------------
//
void TCarbonEvent::Release()
{
	ReleaseEvent( fEvent );
}

//-----------------------------------------------------------------------------------
//	PostToQueue
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::PostToQueue(
	EventQueueRef		inQueue,
	EventPriority		inPriority )
{
	return PostEventToQueue( inQueue, fEvent, inPriority );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	EventParamType		inType,
	UInt32				inSize,
	const void*			inData )
{
	return SetEventParameter( fEvent, inName, inType, inSize, inData );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	EventParamType		inType,
	UInt32				inBufferSize,
	void*				outData )
{
	return GetEventParameter( fEvent, inName, inType, NULL, inBufferSize, NULL, outData );
}

//-----------------------------------------------------------------------------------
//	GetParameterType
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameterType(
	EventParamName		inName,
	EventParamType*		outType )
{
	return GetEventParameter( fEvent, inName, typeWildCard, outType, 0, NULL, NULL );
}

//-----------------------------------------------------------------------------------
//	GetParameterSize
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameterSize(
	EventParamName		inName,
	UInt32*				outSize )
{
	return GetEventParameter( fEvent, inName, typeWildCard, NULL, 0, outSize, NULL );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	Boolean				inValue )
{
	return SetParameter<Boolean>( inName, typeBoolean, inValue );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	Boolean*			outValue )
{
	return GetParameter<Boolean>( inName, typeBoolean, outValue );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	bool				inValue )
{
	return SetParameter<Boolean>( inName, typeBoolean, (Boolean) inValue );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	bool*				outValue )
{
	return GetParameter<Boolean>( inName, sizeof( Boolean ), (Boolean*) outValue );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	Point				inPt )
{
	return SetParameter<Point>( inName, typeQDPoint, inPt );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	Point*				outPt )
{
	return GetParameter<Point>( inName, typeQDPoint, outPt );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	const HIPoint&		inPt )
{
	return SetParameter<HIPoint>( inName, typeHIPoint, inPt );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	HIPoint*			outPt )
{
	return GetParameter<HIPoint>( inName, typeHIPoint, outPt );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	const Rect&			inRect )
{
	return SetParameter<Rect>( inName, typeQDRectangle, inRect );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	Rect*				outRect )
{
	return GetParameter<Rect>( inName, typeQDRectangle, outRect );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	const HIRect&		inRect )
{
	return SetParameter<HIRect>( inName, typeHIRect, inRect );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	HIRect*				outRect )
{
	return GetParameter<HIRect>( inName, typeHIRect, outRect );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	const HISize&		inSize )
{
	return SetParameter<HISize>( inName, typeHISize, inSize );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	HISize*				outSize )
{
	return GetParameter<HISize>( inName, typeHISize, outSize );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	RgnHandle			inRegion )
{
	return SetParameter<RgnHandle>( inName, typeQDRgnHandle, inRegion );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	RgnHandle*			outRegion )
{
	return GetParameter<RgnHandle>( inName, typeQDRgnHandle, outRegion );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	WindowRef			inWindow )
{
	return SetParameter<WindowRef>( inName, typeWindowRef, inWindow );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	WindowRef*			outWindow )
{
	return GetParameter<WindowRef>( inName, typeWindowRef, outWindow );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	ControlRef			inControl )
{
	return SetParameter<ControlRef>( inName, typeControlRef, inControl );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	ControlRef* outControl )
{
	return GetParameter<ControlRef>( inName, typeControlRef, outControl );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	MenuRef				inMenu )
{
	return SetParameter<MenuRef>( inName, typeMenuRef, inMenu );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	MenuRef*			outMenu )
{
	return GetParameter<MenuRef>( inName, typeMenuRef, outMenu );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	DragRef				inDrag )
{
	return SetParameter<DragRef>( inName, typeDragRef, inDrag );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	DragRef*			outDrag )
{
	return GetParameter<DragRef>( inName, typeDragRef, outDrag );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	UInt32				inValue )
{
	return SetParameter<UInt32>( inName, typeUInt32, inValue );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	UInt32*				outValue )
{
	return GetParameter<UInt32>( inName, typeUInt32, outValue );
}

//-----------------------------------------------------------------------------------
//	SetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::SetParameter(
	EventParamName		inName,
	const HICommand&	inValue )
{
	return SetParameter<HICommand>( inName, typeHICommand, inValue );
}

//-----------------------------------------------------------------------------------
//	GetParameter
//-----------------------------------------------------------------------------------
//
OSStatus TCarbonEvent::GetParameter(
	EventParamName		inName,
	HICommand*			outValue )
{
	return GetParameter<HICommand>( inName, typeHICommand, outValue );
}
