package org.varnerlab.kwatee.grnmodel.models;

import java.util.Hashtable;

/**
 * Copyright (c) 2015 Varnerlab,
 * School of Chemical Engineering,
 * Purdue University, West Lafayette IN 46077 USA.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * <p>
 * Created by jeffreyvarner on 10/19/15.
 */
public class VLCGSignalTransductionControlModel implements VLCGGRNModelComponent {

    // instance variables -
    private Hashtable _reaction_component_table = new Hashtable();

    // hastable keys -
    public static final String SIGNAL_TRANSDUCTION_CONTROL_NAME = "STC_NAME";
    public static final String SIGNAL_TRANSDUCTION_CONTROL_ACTOR = "STC_ACTOR";
    public static final String SIGNAL_TRANSDUCTION_CONTROL_TARGET = "STC_TARGET";
    public static final String SIGNAL_TRANSDUCTION_CONTROL_TYPE = "STC_TYPE";
    public static final String SIGNAL_TRANSDUCTION_CONTROL_RAW_STRING = "raw_control_string";

    @Override
    public Object getModelComponent(String key) throws Exception {

        if (key == null || _reaction_component_table.containsKey(key) == false){
            throw new Exception("ERROR: Missing metabolic reaction component. Can't find key = "+key);
        }

        return _reaction_component_table.get(key);
    }

    @Override
    public void setModelComponent(String key, Object value) {

        if (key == null && value == null){
            return;
        }

        // store the reaction component -
        _reaction_component_table.put(key,value);
    }

    public Object doExecute() throws Exception {
        return null;
    }
}
