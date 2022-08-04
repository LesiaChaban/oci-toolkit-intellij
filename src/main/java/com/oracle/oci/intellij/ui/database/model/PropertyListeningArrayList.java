package com.oracle.oci.intellij.ui.database.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class PropertyListeningArrayList<E extends EventSource> extends AbstractList<E>
        implements PropertyChangeListener {
    private List<E> wrapped = new ArrayList<>();
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.pcs.firePropertyChange(evt);
    }

    @Override
    public E get(int index) {
        return this.wrapped.get(index);
    }

    @Override
    public int size() {
        return this.wrapped.size();
    }

    @Override
    public boolean add(E e) {
        int curIndex = this.wrapped.size();
        if (this.wrapped.add(e)) {
            e.addPropertyChangeListener(this);
            this.pcs.fireIndexedPropertyChange("acl", curIndex, null, e); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    @Override
    public E set(int index, E element) {
        E oldValue = this.wrapped.set(index, element);
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(this);
        }
        this.pcs.fireIndexedPropertyChange("acl", index, oldValue, element); //$NON-NLS-1$
        return oldValue;
    }

    @Override
    public E remove(int index) {
        E oldValue = this.wrapped.remove(index);
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(this);
        }
        this.pcs.fireIndexedPropertyChange("acl", index, oldValue, null); //$NON-NLS-1$
        return oldValue;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

}