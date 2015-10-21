package org.varnerlab.kwatee.grnmodel;

import org.varnerlab.kwatee.foundation.VLCGCopyrightFactory;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

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
public class VLCGJuliaGRNModelDelegate {

    // instance variables -
    private VLCGCopyrightFactory copyrightFactory = VLCGCopyrightFactory.getSharedInstance();
    private java.util.Date today = Calendar.getInstance().getTime();
    private SimpleDateFormat date_formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    public VLCGJuliaGRNModelDelegate() {

        // init me -
        _init();
    }

    private void _init(){
    }


    public String buildDriverFunctionBuffer(VLCGGRNModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // String buffer -
        StringBuffer driver = new StringBuffer();

        // We need to get the imports -
        String balance_filename = property_tree.lookupKwateeBalanceFunctionName()+".jl";
        driver.append("include(\"");
        driver.append(balance_filename);
        driver.append("\")\n");
        driver.append("using Sundials;\n");
        driver.append("\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        driver.append(copyright);

        // Get the function name -
        String function_name = property_tree.lookupKwateeDriverFunctionName();
        driver.append("function ");
        driver.append(function_name);
        driver.append("(TSTART,TSTOP,Ts,data_dictionary)\n");

        driver.append("# ----------------------------------------------------------------------------------- #\n");
        driver.append("# ");
        driver.append(function_name);
        driver.append(".jl was generated using the Kwatee code generation system.\n");
        driver.append("# ");
        driver.append(function_name);
        driver.append(": Solves model equations from TSTART to TSTOP given parameters in data_dictionary.\n");
        driver.append("# Username: ");
        driver.append(property_tree.lookupKwateeModelUsername());
        driver.append("\n");
        driver.append("# Type: ");
        driver.append(property_tree.lookupKwateeModelType());
        driver.append("\n");
        driver.append("# Version: ");
        driver.append(property_tree.lookupKwateeModelVersion());
        driver.append("\n");
        driver.append("# Generation timestamp: ");
        driver.append(date_formatter.format(today));
        driver.append("\n");
        driver.append("# \n");
        driver.append("# Input arguments: \n");
        driver.append("# TSTART  - Time start \n");
        driver.append("# TSTOP  - Time stop \n");
        driver.append("# Ts - Time step \n");
        driver.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        driver.append("# \n");
        driver.append("# Return arguments: \n");
        driver.append("# TSIM - Simulation time vector \n");
        driver.append("# X - Simulation state array (NTIME x NSPECIES) \n");
        driver.append("# ----------------------------------------------------------------------------------- #\n");
        driver.append("\n");

        driver.append("# Get required stuff from DataFile struct -\n");
        driver.append("TSIM = [TSTART:Ts:TSTOP];\n");
        driver.append("initial_condition_vector = data_dictionary[\"INITIAL_CONDITION_ARRAY\"];\n");
        driver.append("\n");

        driver.append("# Call the ODE solver - \n");
        driver.append("fbalances(t,y,ydot) = ");
        driver.append(property_tree.lookupKwateeBalanceFunctionName());
        driver.append("(t,y,ydot,data_dictionary);\n");
        driver.append("X = Sundials.cvode(fbalances,initial_condition_vector,TSIM);\n");
        driver.append("\n");
        driver.append("return (TSIM,X);\n");

        // last line -
        driver.append("end\n");

        // return the populated buffer -
        return driver.toString();
    }


    public String buildKineticsFunctionBuffer(VLCGGRNModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Get the kinetics function name -
        String kinetics_function_name = property_tree.lookupKwateeKineticsFunctionName();

        // Propulate the buffer -
        buffer.append("function ");
        buffer.append(kinetics_function_name);
        buffer.append("(t,x,data_dictionary)\n");
        buffer.append("# --------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(kinetics_function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
        buffer.append("# Username: ");
        buffer.append(property_tree.lookupKwateeModelUsername());
        buffer.append("\n");
        buffer.append("# Type: ");
        buffer.append(property_tree.lookupKwateeModelType());
        buffer.append("\n");
        buffer.append("# Version: ");
        buffer.append(property_tree.lookupKwateeModelVersion());
        buffer.append("\n");
        buffer.append("# Generation timestamp: ");
        buffer.append(date_formatter.format(today));
        buffer.append("\n");
        buffer.append("# \n");
        buffer.append("# Input arguments: \n");
        buffer.append("# t  - current time \n");
        buffer.append("# x  - state vector \n");
        buffer.append("# data_dictionary - parameter vector \n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# rate_vector - rate vector \n");
        buffer.append("# --------------------------------------------------------------------- #\n");
        buffer.append("# \n");
        buffer.append("# Alias the species vector - \n");

        // Get the species id vector -
        Vector<String> species_symbol_vector = model_tree.getSpeciesSymbolsFromGRNModel();
        int number_of_species = species_symbol_vector.size();
        for (int species_index = 0;species_index<number_of_species;species_index++){

            // Species -
            String symbol = species_symbol_vector.elementAt(species_index);

            // write the buffer line -
            buffer.append(symbol);
            buffer.append(" = x[");
            buffer.append(species_index+1);
            buffer.append("];\n");
        }

        // last line -
        buffer.append("\n");
        buffer.append("# return the kinetics vector -\n");
        buffer.append("return rate_vector;\n");
        buffer.append("end\n");

        // return the buffer -
        return buffer.toString();
    }

    public String buildBalanceFunctionBuffer(VLCGGRNModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuffer massbalances = new StringBuffer();

        // Get the balance function name -
        String balance_function_name = property_tree.lookupKwateeBalanceFunctionName();

        // Get/Set the kinetics function import -
        String kinetics_function_name = property_tree.lookupKwateeKineticsFunctionName();
        massbalances.append("include(\"");
        massbalances.append(kinetics_function_name);
        massbalances.append(".jl\");\n");

        // Get/Set the kinetics function import -
        String control_function_name = property_tree.lookupKwateeControlFunctionName();
        massbalances.append("include(\"");
        massbalances.append(control_function_name);
        massbalances.append(".jl\");\n");
        massbalances.append("\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        massbalances.append(copyright);

        // Fill in the buffer -
        massbalances.append("function ");
        massbalances.append(balance_function_name);
        massbalances.append("(t,x,dxdt_vector,data_dictionary)\n");
        massbalances.append("# ---------------------------------------------------------------------- #\n");
        massbalances.append("# ");
        massbalances.append(balance_function_name);
        massbalances.append(".jl was generated using the Kwatee code generation system.\n");
        massbalances.append("# Username: ");
        massbalances.append(property_tree.lookupKwateeModelUsername());
        massbalances.append("\n");
        massbalances.append("# Type: ");
        massbalances.append(property_tree.lookupKwateeModelType());
        massbalances.append("\n");
        massbalances.append("# Version: ");
        massbalances.append(property_tree.lookupKwateeModelVersion());
        massbalances.append("\n");
        massbalances.append("# Generation timestamp: ");
        massbalances.append(date_formatter.format(today));
        massbalances.append("\n");
        massbalances.append("# \n");
        massbalances.append("# Arguments: \n");
        massbalances.append("# t  - current time \n");
        massbalances.append("# x  - state vector \n");
        massbalances.append("# dxdt_vector - right hand side vector \n");
        massbalances.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        massbalances.append("# ---------------------------------------------------------------------- #\n");
        massbalances.append("\n");
        massbalances.append("# Call the kinetics function - \n");
        massbalances.append("(rate_vector) = ");
        massbalances.append(kinetics_function_name);
        massbalances.append("(t,x,data_dictionary);\n");

        massbalances.append("\n");
        massbalances.append("# Call the control function - \n");
        massbalances.append("(rate_vector) = ");
        massbalances.append(control_function_name);
        massbalances.append("(t,x,rate_vector,data_dictionary);\n");
        massbalances.append("\n");

        // check - is this model large scale optimized?
        if (property_tree.isKwateeModelLargeScaleOptimized() == true){

            // build explicit list of balance equations -

        }
        else {

            // balance are encoded as matrix vector product -
            massbalances.append("# Encode the balance equations as a matrix vector product - \n");
            massbalances.append("S = data_dictionary[\"STOICHIOMETRIC_MATRIX\"];\n");
            massbalances.append("tmp_vector = S*rate_vector;\n");
            massbalances.append("number_of_states = length(tmp_vector);\n");
            massbalances.append("for state_index in [1:number_of_states]\n");
            massbalances.append("\tdxdt_vector[state_index] = tmp_vector[state_index];\n");
            massbalances.append("end");
            massbalances.append("\n");
        }

        // last line -
        massbalances.append("\n");
        massbalances.append("end\n");

        // return the buffer -
        return massbalances.toString();
    }

    public String buildDataDictionaryBuffer(VLCGGRNModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // String buffer -
        StringBuffer buffer = new StringBuffer();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Get the function name -
        String function_name = property_tree.lookupKwateeDataDictionaryFunctionName();
        buffer.append("function ");
        buffer.append(function_name);
        buffer.append("(TSTART,TSTOP,Ts)\n");

        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(": Stores model parameters as key - value pairs in a Julia Dict() \n");
        buffer.append("# Username: ");
        buffer.append(property_tree.lookupKwateeModelUsername());
        buffer.append("\n");
        buffer.append("# Type: ");
        buffer.append(property_tree.lookupKwateeModelType());
        buffer.append("\n");
        buffer.append("# Version: ");
        buffer.append(property_tree.lookupKwateeModelVersion());
        buffer.append("\n");
        buffer.append("# Generation timestamp: ");
        buffer.append(date_formatter.format(today));
        buffer.append("\n");
        buffer.append("# \n");
        buffer.append("# Input arguments: \n");
        buffer.append("# TSTART  - Time start \n");
        buffer.append("# TSTOP  - Time stop \n");
        buffer.append("# Ts - Time step \n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("\n");
        buffer.append("# Load the stoichiometric matrix - \n");

        // Get the path to the stoichiometric matrix -
        String fully_qualified_stoichiometric_matrix_path = property_tree.lookupKwateeStoichiometricMatrixFilePath();
        buffer.append("S = float(open(readdlm,");
        buffer.append("\"");
        buffer.append(fully_qualified_stoichiometric_matrix_path);
        buffer.append("\"));\n");
        buffer.append("(NSPECIES,NREACTIONS) = size(S);\n");

        // Get the species id vector -
        buffer.append("\n");
        buffer.append("# Formulate the initial condition array - \n");
        buffer.append("initial_condition_array = Float64[];\n");
        Vector<String> species_symbol_vector = model_tree.getSpeciesSymbolsFromGRNModel();
        int number_of_species = species_symbol_vector.size();
        for (int species_index = 0;species_index<number_of_species;species_index++){

            // Species -
            String symbol = species_symbol_vector.elementAt(species_index);
            String initial_amount = model_tree.getInitialAmountForSpeciesWithSymbol(symbol);

            // write ic record -
            buffer.append("push!(initial_condition_array,");
            buffer.append(initial_amount);
            buffer.append(");\t");
            buffer.append("#\t");
            buffer.append(species_index+1);
            buffer.append("\t id:");
            buffer.append(symbol);
            buffer.append("\t symbol:");
            buffer.append(symbol);
            buffer.append("\n");
        }

        buffer.append("\n");
        buffer.append("# ---------------------------- DO NOT EDIT BELOW THIS LINE -------------------------- #\n");
        buffer.append("data_dictionary = Dict();\n");
        buffer.append("data_dictionary[\"STOICHIOMETRIC_MATRIX\"] = S;\n");
        buffer.append("data_dictionary[\"RATE_CONSTANT_ARRAY\"] = rate_constant_array;\n");
        buffer.append("data_dictionary[\"SATURATION_CONSTANT_ARRAY\"] = saturation_constant_array;\n");
        buffer.append("data_dictionary[\"INITIAL_CONDITION_ARRAY\"] = initial_condition_array;\n");
        buffer.append("data_dictionary[\"CONTROL_PARAMETER_ARRAY\"] = control_parameter_array;\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");

        // last line -
        buffer.append("return data_dictionary;\n");
        buffer.append("end\n");

        // return the buffer -
        return buffer.toString();
    }

    public String buildControlFunctionBuffer(VLCGGRNModelTreeWrapper model_tree,VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the control function name -
        String control_function_name = property_tree.lookupKwateeControlFunctionName();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Fill in the buffer -
        buffer.append("function ");
        buffer.append(control_function_name);
        buffer.append("(t,x,rate_vector,data_dictionary)\n");
        buffer.append("# ---------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(control_function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
        buffer.append("# Username: ");
        buffer.append(property_tree.lookupKwateeModelUsername());
        buffer.append("\n");
        buffer.append("# Type: ");
        buffer.append(property_tree.lookupKwateeModelType());
        buffer.append("\n");
        buffer.append("# Version: ");
        buffer.append(property_tree.lookupKwateeModelVersion());
        buffer.append("\n");
        buffer.append("# Generation timestamp: ");
        buffer.append(date_formatter.format(today));
        buffer.append("\n");
        buffer.append("# \n");
        buffer.append("# Arguments: \n");
        buffer.append("# t  - current time \n");
        buffer.append("# x  - state vector \n");
        buffer.append("# rate_vector - vector of reaction rates \n");
        buffer.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        buffer.append("# ---------------------------------------------------------------------- #\n");
        buffer.append("\n");
        buffer.append("# Set a default value for the allosteric control variables - \n");
        buffer.append("number_of_reactions = length(rate_vector);\n");
        buffer.append("control_vector = ones(number_of_reactions);\n");
        buffer.append("control_parameter_array = data_dictionary[\"CONTROL_PARAMETER_ARRAY\"];\n");
        buffer.append("\n");

        Vector<String> reaction_name_list = model_tree.getListOfReactionNamesFromGRNModelTree();
        Iterator reaction_name_iterator = reaction_name_list.iterator();
        while (reaction_name_iterator.hasNext()){

            // Get the reaction name -
            String reaction_name = (String)reaction_name_iterator.next();

        }

        buffer.append("# Modify the rate_vector with the control variables - \n");
        buffer.append("rate_vector = rate_vector.*control_vector;\n");

        // last line -
        buffer.append("\n");
        buffer.append("# Return the modified rate vector - \n");
        buffer.append("return rate_vector;\n");
        buffer.append("end\n");

        // return the buffer -
        return buffer.toString();
    }

}
