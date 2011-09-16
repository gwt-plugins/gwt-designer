/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.shell.designtime;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Class for wrapping Java things for JavaScript.
 */
public class JavaDispatchImpl implements JavaDispatch {

  private final WeakReference<DispatchIdOracle> dispIdOracleRef;

  private final Object target;

  /**
   * This constructor initializes a dispatcher for handling static members.
   * 
   * @param ccl class loader to use for dispatching member access
   */
  public JavaDispatchImpl(DispatchIdOracle ccl) {
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ccl);
    this.target = null;
  }

  /**
   * This constructor initializes a dispatcher around a particular instance.
   * 
   * @param ccl class loader to use for dispatching member access
   * @param target the instance object to use for dispatching member accesses
   * 
   * @throws NullPointerException if target is null
   */
  public JavaDispatchImpl(DispatchIdOracle ccl, Object target) {
    if (target == null) {
      throw new NullPointerException("target cannot be null");
    }

    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ccl);
    this.target = target;
  }

  /**
   * @param dispId the unique number of a field
   * @return the field
   */
  public Field getField(int dispId) {
    if (dispId < 0) {
      throw new RuntimeException("Field does not exist.");
    }
    Member member = getMember(dispId);

    if (isSyntheticClassMember(member)) {
      try {
        Field f = member.getClass().getDeclaredField("clazz");
        assert f != null;
        return f;
      } catch (SecurityException e) {
      } catch (NoSuchFieldException e) {
      }
      assert false : "Should never get here";
    }
    return (Field) member;
  }

  /**
   * @param dispId the unique number of a field
   * @return true the value of the field
   */
  public Object getFieldValue(int dispId) {
    if (dispId < 0) {
      throw new RuntimeException("Field does not exist.");
    }
    
    Member member = getMember(dispId);

    if (isSyntheticClassMember(member)) {
      return member.getDeclaringClass();
    }

    Field field = (Field) member;
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      // should never, ever happen
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * @param dispId the unique number of a method
   * @return the method
   */
  public MethodAdaptor getMethod(int dispId) {
    if (dispId < 0) {
      throw new RuntimeException("Method does not exist.");
    }
    
    Member m = getMember(dispId);
    if (m instanceof Method) {
      return new MethodAdaptor((Method) m);
    } else if (m instanceof Constructor<?>) {
      return new MethodAdaptor((Constructor<?>) m);
    } else {
      throw new RuntimeException();
    }
  }

  public Object getTarget() {
    return target;
  }

  /**
   * @param dispId the unique number of a method or field
   * @return true if the dispId represents a field
   */
  public boolean isField(int dispId) {
    if (dispId < 0) {
      return false;
    }

    Member member = getMember(dispId);
    return member instanceof Field || isSyntheticClassMember(member);
  }

  /**
   * @param dispId the unique number of a method or field
   * @return true if the dispId represents a method
   */
  public boolean isMethod(int dispId) {
    if (dispId < 0) {
      return false;
    }

    Member m = getMember(dispId);
    return m instanceof Method || m instanceof Constructor<?>;
  }

  /**
   * @param dispId the unique number of a field
   * @param value the value to assign to the field
   */
  public void setFieldValue(int dispId, Object value) {
    Field field = (Field) getMember(dispId);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      // should never, ever happen
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * @param dispId the unique number of a method or field
   * @return the member
   */
  protected Member getMember(int dispId) {
    DispatchIdOracle dispIdOracle = dispIdOracleRef.get();
    if (dispIdOracle == null) {
      return null;
    }
    DispatchClassInfo clsInfo = dispIdOracle.getClassInfoByDispId(dispId);
    return clsInfo.getMember(dispId);
  }
  
  private boolean isSyntheticClassMember(Member member) {
    return member != null && member.getClass().getName().equals("com.google.gwt.dev.shell.SyntheticClassMember");
  }
}
