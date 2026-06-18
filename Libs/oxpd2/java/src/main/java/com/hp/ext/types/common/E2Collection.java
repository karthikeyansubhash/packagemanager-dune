/**
 * (C) Copyright 2022 HP Development Company, L.P.
 * All rights reserved.
 */
package com.hp.ext.types.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

public class E2Collection<T> extends E2Type
{
    protected List<T> items = new ArrayList<T>();

    protected E2Collection(List<T> items)
    {
        if (null != items)
        {
            this.items = new ArrayList<T>(items);
        }
    }

    // TODO: I'm not sure how to do this in java?
    public Class<?> getCollectionOf()
    {
        return this.getClass();
    }

    @JsonValue
    public List<T> getItems()
    {
           return Collections.unmodifiableList(items);

    }

    public void setItems(List<T> items) {
        if (null != items)
        {
            this.items = new ArrayList<T>(items);
        }
    }

    public void add(T item)
    {
        // TODO: synchronize adding/removing?
        items.add(item);
    }

    public void remove(T item)
    {
        // TODO: synchronize adding/removing?
        items.remove(item);
    }

    public void clear()
    {
       // TODO: synchronize adding/removing?
        items.clear();
    }
}
