/*
 * ParameterContextX.java
 *
 * Created on February 13, 2003, 1:51 PM
 */

package com.pcmsolutions.device.EMU.E4.parameter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author  pmeehan
 */
public interface ParameterContext {
    public String getGenerator();

    public List getAllParameterDescriptors();                            // returns List of ParameterDescriptors

    public Map getIdsAndDefaultsAsMap();                           // returns Map of Integer ids -> Integer default values

    public Set getIds();               // returns list of Integer ids

    public int size();

    public GeneralParameterDescriptor getParameterDescriptor(String refName) throws IllegalParameterReferenceException;

    public GeneralParameterDescriptor getParameterDescriptor(Integer id) throws IllegalParameterIdException;

    public String getRefName(Integer id) throws IllegalParameterIdException;

    public Integer getId(String refName) throws IllegalParameterReferenceException;

    public boolean paramExists(Integer id);

    public List getCategories();            // returns List of category strings ( List of "cat;(1..n)subcat")

    public List getPDsForCategory(String cs); // returns ordered list of ParameterDescriptors

    public List getIdsForCategory(String cs); // returns ordered list of ParameterDescriptors
}
