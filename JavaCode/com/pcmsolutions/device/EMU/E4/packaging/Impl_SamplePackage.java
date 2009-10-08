package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.util.ClassUtility;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 02-Dec-2003
 * Time: 07:10:42
 * To change this template use Options | File Templates.
 */
class Impl_SamplePackage implements SamplePackage, Serializable {
    static final long serialVersionUID = 1;

    private IsolatedSample[] samples;
    private Map customObjectMap;
    private SamplePackage.Header header;

    public Impl_SamplePackage(IsolatedSample[] samples) {
        init(samples, null);
    }

    public Impl_SamplePackage(IsolatedSample[] samples, Map customObjectMap) {
        init(samples, customObjectMap);
        File f;
    }

    private void init(IsolatedSample[] samples, Map customObjectMap) {
        this.samples = (IsolatedSample[]) samples.clone();
        if (customObjectMap != null)
            this.customObjectMap = new HashMap(customObjectMap);
    }

    public void setCustomObjectMap(Map customObjectMap) {
        if (customObjectMap != null &&
                !(ClassUtility.instanceCount(customObjectMap.entrySet().toArray(), Serializable.class) == customObjectMap.size())
                || !(ClassUtility.instanceCount(customObjectMap.keySet().toArray(), Serializable.class) == customObjectMap.size())
        )
            throw new IllegalArgumentException("not all custom objects are serializable");
        this.customObjectMap = customObjectMap;
    }

    public IsolatedSample[] getSamples()       // possibly null
    {
        if (samples != null)
            return (IsolatedSample[]) samples.clone();
        else
            return null;
    }

    public void setSamples(IsolatedSample[] samples) {
        this.samples = samples;
    }

    public Map getCustomObjectMap() {
        if (customObjectMap != null)
            return new HashMap(customObjectMap);
        else
            return new HashMap();
    }

    public void setHeader(SamplePackage.Header header) {
        this.header = header;
    }

    public SamplePackage.Header getHeader() {
        return header;
    }
}
