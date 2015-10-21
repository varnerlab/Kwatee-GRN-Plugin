package org.varnerlab.kwatee.grnmodel;

import org.varnerlab.kwatee.foundation.VLCGOutputHandler;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
 * Created by jeffreyvarner on 10/16/15.
 */
public class VLCGWriteJuliaGRNModel implements VLCGOutputHandler {

    // instance variables -
    private VLCGTransformationPropertyTree _transformation_properties_tree = null;
    private VLCGJuliaGRNModelDelegate _delegate_object = new VLCGJuliaGRNModelDelegate();

    @Override
    public void writeResource(Object object) throws Exception {

        // Grab the model tree -
        VLCGGRNModelTreeWrapper model_wrapper = (VLCGGRNModelTreeWrapper)object;

        // Write the model components out, formulate the buffers using methods on the delegate
        // Build the data dictionary -
        String fully_qualified_data_dictionary_path = _transformation_properties_tree.lookupKwateeDataDictionaryFilePath();
        String data_dictionary = _delegate_object.buildDataDictionaryBuffer(model_wrapper,_transformation_properties_tree);
        write(fully_qualified_data_dictionary_path,data_dictionary);

        // Build the driver -
        String fully_qualified_driver_path = _transformation_properties_tree.lookupKwateeDriverFunctionFilePath();
        String driver_buffer = _delegate_object.buildDriverFunctionBuffer(model_wrapper,_transformation_properties_tree);
        write(fully_qualified_driver_path,driver_buffer);

        // Build the balance equations -
        String fully_qualified_balance_path = _transformation_properties_tree.lookupKwateeBalanceFunctionFilePath();
        String balance_buffer = _delegate_object.buildBalanceFunctionBuffer(model_wrapper,_transformation_properties_tree);
        write(fully_qualified_balance_path,balance_buffer);

        // Build the kinetics equations -
        String fully_qualified_kinetics_path = _transformation_properties_tree.lookupKwateeKineticsFunctionFilePath();
        String kinetics_buffer = _delegate_object.buildKineticsFunctionBuffer(model_wrapper,_transformation_properties_tree);
        write(fully_qualified_kinetics_path,kinetics_buffer);

        // Build the allosteric control equations -
        String fully_qualified_allosteric_control_path = _transformation_properties_tree.lookupKwateeControlFunctionFilePath();
        String control_buffer = _delegate_object.buildControlFunctionBuffer(model_wrapper,_transformation_properties_tree);
        write(fully_qualified_allosteric_control_path,control_buffer);
    }

    @Override
    public void setPropertiesTree(VLCGTransformationPropertyTree properties_tree) {

        if (properties_tree == null){
            return;
        }

        _transformation_properties_tree = properties_tree;
    }


    // private methods -
    private void write(String path,String buffer) throws Exception {

        // Create writer
        File oFile = new File(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(oFile));

        // Write buffer to file system and close writer
        writer.write(buffer);
        writer.close();
    }
}
