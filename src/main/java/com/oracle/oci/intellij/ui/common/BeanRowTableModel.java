package com.oracle.oci.intellij.ui.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class BeanRowTableModel<BeanType> extends AbstractTableModel {

  private static final long serialVersionUID = -9119870013663268492L;
  private final List<String> propertyNames;
  private final Class<BeanType> beanClass;
  List<BeanType> beans = Collections.emptyList();
  Map<String, PropertyDescriptor> nameToProperty = new HashMap<>();
  private List<String> columnNames;

  public BeanRowTableModel(Class<BeanType> beanClass, List<String> propertyNames) {
    this.beanClass = beanClass;
    this.propertyNames = propertyNames;
//    this.columnsInLower = columnNames.stream().map((s) -> s.toLowerCase()).collect(Collectors.toList());
    initBeanDescriptor(this.beanClass, this.propertyNames);
    this.columnNames = new ArrayList<>(propertyNames);
    for (int i = 0; i < columnNames.size(); i++) {
      columnNames.set(i, beanPropertyToDisplayName(columnNames.get(i)));
    }
  }

  private void initBeanDescriptor(Class<BeanType> beanClass,
                                  List<String> columnNames) {
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(this.beanClass);
      PropertyDescriptor[] propertyDescriptors =
        beanInfo.getPropertyDescriptors();
      List<PropertyDescriptor> pds = Arrays.asList(propertyDescriptors);
      this.nameToProperty = new HashMap<>();
      for (String propName : columnNames) {
        for (PropertyDescriptor pd : pds) {
          if (pd.getName().equals(propName)) {
            this.nameToProperty.put(propName, pd);
          }
        }
//        UIUtil.fireNotification(NotificationType.WARNING, "Missing column: " + propName, propName);
      }
     } catch (IntrospectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public String getColumnName(int column) {
    return this.columnNames.get(column);
  }

  @Override
  public int getRowCount() {
    return this.beans.size();
  }

  @Override
  public int getColumnCount() {
    return propertyNames.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < this.beans.size()) {
      BeanType beanType = this.beans.get(rowIndex);
      if (columnIndex < this.propertyNames.size()) {
        String colName = this.propertyNames.get(columnIndex);
        PropertyDescriptor pd = nameToProperty.get(colName);
          if (pd != null) {
            return readProperty(beanType, pd);
          }
          else
          {
            UIUtil.warn("Missing property name in bean table: "+colName);
          }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T> T readProperty(BeanType beanType,
                         PropertyDescriptor pd) {
    try {
      Method readMethod = pd.getReadMethod();
      Object invoke = readMethod.invoke(beanType);
      if (pd.getPropertyType().isInstance(invoke)) {
        return (T) invoke;
      }
    }
    catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void setBeans(List<BeanType> beans) {
    this.beans = beans;
  }

  public List<String> getColumnNames() {
    return Collections.unmodifiableList(columnNames);
  }

  public Class<BeanType> getBeanClass() {
    return beanClass;
  }
  
  public static String beanPropertyToDisplayName(String propName) {
    char[] propNameAsChars = propName.toCharArray();
    StringBuilder displayStr = new StringBuilder();
    for (int i = 0; i < propNameAsChars.length; i++) {
      char nextChar = propNameAsChars[i];
      if (i == 0) {
        displayStr.append(Character.toUpperCase(nextChar));
      }
      else {
        if (Character.isUpperCase(nextChar)) {
          displayStr.append(' ');
        }
        displayStr.append(nextChar);
      }
    }
    
    return displayStr.toString();
  }
}
