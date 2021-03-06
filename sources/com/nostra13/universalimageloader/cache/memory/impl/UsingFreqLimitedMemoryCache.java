package com.nostra13.universalimageloader.cache.memory.impl;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.memory.LimitedMemoryCache;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UsingFreqLimitedMemoryCache extends LimitedMemoryCache {
    private final Map<Bitmap, Integer> usingCounts = Collections.synchronizedMap(new HashMap());

    public UsingFreqLimitedMemoryCache(int i) {
        super(i);
    }

    public boolean put(String str, Bitmap bitmap) {
        if (super.put(str, bitmap) == null) {
            return false;
        }
        this.usingCounts.put(bitmap, Integer.valueOf(0));
        return true;
    }

    public Bitmap get(String str) {
        str = super.get(str);
        if (str != null) {
            Integer num = (Integer) this.usingCounts.get(str);
            if (num != null) {
                this.usingCounts.put(str, Integer.valueOf(num.intValue() + 1));
            }
        }
        return str;
    }

    public Bitmap remove(String str) {
        Bitmap bitmap = super.get(str);
        if (bitmap != null) {
            this.usingCounts.remove(bitmap);
        }
        return super.remove(str);
    }

    public void clear() {
        this.usingCounts.clear();
        super.clear();
    }

    protected int getSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    protected Bitmap removeNext() {
        Bitmap bitmap;
        Set<Entry> entrySet = this.usingCounts.entrySet();
        synchronized (this.usingCounts) {
            bitmap = null;
            Integer num = null;
            for (Entry entry : entrySet) {
                if (bitmap == null) {
                    bitmap = (Bitmap) entry.getKey();
                    num = (Integer) entry.getValue();
                } else {
                    Integer num2 = (Integer) entry.getValue();
                    if (num2.intValue() < num.intValue()) {
                        bitmap = (Bitmap) entry.getKey();
                        num = num2;
                    }
                }
            }
        }
        this.usingCounts.remove(bitmap);
        return bitmap;
    }

    protected Reference<Bitmap> createReference(Bitmap bitmap) {
        return new WeakReference(bitmap);
    }
}
