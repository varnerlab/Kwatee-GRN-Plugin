package org.varnerlab.kwatee.grnmodel;

import org.varnerlab.kwatee.foundation.VLCGCopyrightFactory;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;
import org.varnerlab.kwatee.grnmodel.models.VLCGSimpleControlLogicModel;
import org.varnerlab.kwatee.grnmodel.models.VLCGSimpleSpeciesModel;

import java.io.StringReader;
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


    public void buildStoichiometricMatrix(double[][] dblSTMatrix,VLCGGRNModelTreeWrapper model_wrapper) throws Exception {

        // Get the dimension of the system -
        int NUMBER_OF_SPECIES = 0;
        int NUMBER_OF_RATES = 0;

        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumberOfSpeciesFromGRNModelTree();
        NUMBER_OF_RATES = (int)model_wrapper.getNumberOfReactionsFromGRNModelTree();

        // Go through and put everything as zeros by default -
        for (int scounter = 0;scounter<NUMBER_OF_SPECIES;scounter++) {
            for (int rcounter = 0;rcounter<NUMBER_OF_RATES;rcounter++) {
                dblSTMatrix[scounter][rcounter] = 0.0;
            }
        }

        // Get the list of species -
        Vector<String> species_vector = model_wrapper.getSpeciesSymbolsFromGRNModel();
        Vector<String> reaction_vector = model_wrapper.getListOfReactionNamesFromGRNModelTree();
        Iterator<String> species_iterator = species_vector.iterator();
        Iterator<String> reaction_iterator = reaction_vector.iterator();
        int reaction_index = 0;
        while (reaction_iterator.hasNext()){

            // Get the reaction name -
            String reaction_name = reaction_iterator.next();

            // what are the reactants for this reaction -
            Vector<VLCGSimpleSpeciesModel> reactant_species_model_vector = model_wrapper.getReactantsForReactionWithName(reaction_name);
            Vector<VLCGSimpleSpeciesModel> product_species_model_vector = model_wrapper.getProductsForReactionWithName(reaction_name);

            // process the reactants -
            Iterator<VLCGSimpleSpeciesModel> reactant_iterator = reactant_species_model_vector.iterator();
            while (reactant_iterator.hasNext()){

                // Get the species model -
                VLCGSimpleSpeciesModel species_model = reactant_iterator.next();

                // Get the data from the model -
                String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                String coefficient = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_COEFFICIENT);


                if (!symbol.equalsIgnoreCase("[]")){

                    System.out.println("Processing "+reaction_name+" species = "+symbol);

                    // lookup the species index -
                    int species_index = species_vector.indexOf(symbol);
                    dblSTMatrix[species_index][reaction_index] = Double.parseDouble(coefficient);

                }
            }

            // process the products -
            Iterator<VLCGSimpleSpeciesModel> product_iterator = product_species_model_vector.iterator();
            while (product_iterator.hasNext()){

                // Get the species model -
                VLCGSimpleSpeciesModel species_model = product_iterator.next();

                // Get the data from the model -
                String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                String coefficient = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_COEFFICIENT);



                if (!symbol.equalsIgnoreCase("[]")){

                    System.out.println("Processing "+reaction_name+" species = "+symbol);

                    // lookup the species index -
                    int species_index = species_vector.indexOf(symbol);
                    dblSTMatrix[species_index][reaction_index] = Double.parseDouble(coefficient);
                }
            }

            // update the reaction counter -
            reaction_index++;
        }
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

        buffer.append("\n");
        buffer.append("# Formulate the kinetic rate vector - \n");
        buffer.append("rate_constant_array = data_dictionary[\"RATE_CONSTANT_ARRAY\"];\n");
        buffer.append("saturation_constant_array = data_dictionary[\"SATURATION_CONSTANT_ARRAY\"];\n");
        buffer.append("rate_vector = Float64[];\n");
        buffer.append("\n");

        // Get list of reaction names -
        Vector<String> reaction_name_vector = model_tree.getListOfReactionNamesFromGRNModelTree();
        Iterator<String> reaction_name_iterator = reaction_name_vector.iterator();
        int reaction_counter = 1;
        while (reaction_name_iterator.hasNext()){

            // Get the reaction_name -
            String reaction_name = reaction_name_iterator.next();

            // Get comment for this reaction -
            String comment = model_tree.buildReactionCommentStringForReactionWithName(reaction_name);

            // set the rate constants -
            buffer.append("# ");
            buffer.append(reaction_counter);
            buffer.append(" ");
            buffer.append(comment);
            buffer.append("\n");
            buffer.append("tmp = rate_constant_array[");
            buffer.append(reaction_counter);
            buffer.append("]");

            // Get the reactants for this reaction -
            if (model_tree.isThisASignalTransductionReaction(reaction_name)){

                // get the reactants -
                Vector<VLCGSimpleSpeciesModel> species_model_vector = model_tree.getReactantsForSignalTransductionReactionWithName(reaction_name);
                Iterator<VLCGSimpleSpeciesModel> species_model_iterator = species_model_vector.iterator();

                if (model_tree.isThisADegradationReaction(reaction_name)){

                    // ok, we have a degrdation reaction - this will get generated as first order -
                    while (species_model_iterator.hasNext()) {

                        // Get species model -
                        VLCGSimpleSpeciesModel species_model = species_model_iterator.next();

                        // Get the index for this species_model -
                        String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                        if (!symbol.equalsIgnoreCase("[]")){

                            int species_index = species_symbol_vector.indexOf(symbol);

                            // add the line -
                            buffer.append("*(");
                            buffer.append(symbol);
                            buffer.append(")");
                        }
                    }

                    // add ; and newline -
                    buffer.append(";\n");
                    buffer.append("push!(rate_vector,tmp);\n");
                    buffer.append("\n");
                }
                else {

                    while (species_model_iterator.hasNext()){

                        // Get species model -
                        VLCGSimpleSpeciesModel species_model = species_model_iterator.next();

                        // Get the index for this species_model -
                        String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                        if (!symbol.equalsIgnoreCase("[]")){

                            int species_index = species_symbol_vector.indexOf(symbol);

                            // add the line -
                            buffer.append("*(");
                            buffer.append(symbol);
                            buffer.append(")/(saturation_constant_array[");
                            buffer.append(reaction_counter);
                            buffer.append(",");
                            buffer.append(species_index+1);
                            buffer.append("] + ");
                            buffer.append(symbol);
                            buffer.append(")");

                        }
                    }

                    // add ; and newline -
                    buffer.append(";\n");
                    buffer.append("push!(rate_vector,tmp);\n");
                    buffer.append("\n");
                }
            }
            else if (model_tree.isThisAGeneExpressionReaction(reaction_name)){

                // Get the reactants -
                Vector<VLCGSimpleSpeciesModel> species_model_vector = model_tree.getReactantsForGeneExpressionReactionWithName(reaction_name);
                Iterator<VLCGSimpleSpeciesModel> species_model_iterator = species_model_vector.iterator();
                while (species_model_iterator.hasNext()){

                    // Get species model -
                    VLCGSimpleSpeciesModel species_model = species_model_iterator.next();

                    // Get the symbol -
                    String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                    buffer.append("*");
                    buffer.append(symbol);
                }

                // add ; and newline -
                buffer.append(";\n");
                buffer.append("push!(rate_vector,tmp);\n");
                buffer.append("\n");
            }
            else if (model_tree.isThisATranslationReaction(reaction_name)){


                // Get the reactants -
                Vector<VLCGSimpleSpeciesModel> species_model_vector = model_tree.getReactantsForTranslationReactionWithName(reaction_name);
                Iterator<VLCGSimpleSpeciesModel> species_model_iterator = species_model_vector.iterator();
                while (species_model_iterator.hasNext()){

                    // Get species model -
                    VLCGSimpleSpeciesModel species_model = species_model_iterator.next();

                    // Get the symbol -
                    String symbol = (String)species_model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);
                    buffer.append("*");
                    buffer.append(symbol);
                }

                // add ; and newline -
                buffer.append(";\n");
                buffer.append("push!(rate_vector,tmp);\n");
                buffer.append("\n");
            }


            // update the counter -
            reaction_counter++;
        }

        // last line -
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
        massbalances.append("# Correct nagative x's = throws errors in control even if small - \n");
        massbalances.append("idx = find(x->(x<0),x);\n");
        massbalances.append("x[idx] = 0.0;\n");
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
            massbalances.append("maximum_specific_growth_rate = data_dictionary[\"MAXIMUM_SPECIFIC_GROWTH_RATE\"];\n");
            massbalances.append("S = data_dictionary[\"STOICHIOMETRIC_MATRIX\"];\n");
            massbalances.append("dilution_selection_matrix = data_dictionary[\"DILUTION_SELECTION_MATRIX\"];\n");
            massbalances.append("tau_array = data_dictionary[\"TIME_CONSTANT_ARRAY\"];\n");
            massbalances.append("tmp_vector = S*rate_vector;\n");
            massbalances.append("number_of_states = length(tmp_vector);\n");
            massbalances.append("for state_index in [1:number_of_states]\n");
            massbalances.append("\tdxdt_vector[state_index] = tmp_vector[state_index] - maximum_specific_growth_rate*(dilution_selection_matrix[state_index,state_index])*(x[state_index]);\n");
            massbalances.append("\tdxdt_vector[state_index] = tau_array[state_index]*dxdt_vector[state_index];\n");
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

        // How many genes do we have in the model?
        buffer.append("# How many genes do we have in the model? - \n");
        buffer.append("number_of_genes = ");
        buffer.append(model_tree.getNumberOfGenesFromGRNModelTree());
        buffer.append(";\n");


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
            buffer.append("\t");
            buffer.append(symbol);
            buffer.append("\n");
        }

        // Get the species id vector -
        buffer.append("\n");
        buffer.append("# Formulate the time constant array - \n");
        buffer.append("time_constant_array = Float64[];\n");
        species_symbol_vector = model_tree.getSpeciesSymbolsFromGRNModel();
        number_of_species = species_symbol_vector.size();
        for (int species_index = 0;species_index<number_of_species;species_index++){

            // Species -
            String symbol = species_symbol_vector.elementAt(species_index);
            String initial_amount = model_tree.getInitialAmountForSpeciesWithSymbol(symbol);
            String species_type = model_tree.getSpeciesTypeForSpeciesWithName(symbol);

            if (species_type.equalsIgnoreCase("MRNA") == true){
                buffer.append("push!(time_constant_array,0.1");
            }
            else {
                buffer.append("push!(time_constant_array,1.0");
            }

            // write ic record -
            buffer.append(");\t");
            buffer.append("#\t");
            buffer.append(species_index+1);
            buffer.append("\t time constant: ");
            buffer.append(symbol);
            buffer.append("\n");
        }


        buffer.append("\n");
        buffer.append("# Formulate the rate constant array - \n");
        buffer.append("rate_constant_array = Float64[];\n");
        Vector<String> reaction_name_list = model_tree.getListOfReactionNamesFromGRNModelTree();
        Iterator<String> reaction_name_iterator = reaction_name_list.iterator();
        int reaction_counter = 1;
        while (reaction_name_iterator.hasNext()){

            // get the reaction name -
            String reaction_name = reaction_name_iterator.next();

            // Get the comment for this reaction -
            String comment = model_tree.buildReactionCommentStringForReactionWithName(reaction_name);

            // ok, let's check to see if this is a degradation reaction -
            float default_parameter_value = 1.0f;
            if (model_tree.isThisADegradationReaction(reaction_name) == true){
                default_parameter_value = 0.1f;
            }

            // write the line -
            buffer.append("push!(rate_constant_array,");
            buffer.append(default_parameter_value);
            buffer.append(");\t# ");
            buffer.append(reaction_counter);
            buffer.append("\t");
            buffer.append(comment);
            buffer.append("\n");

            // update the counter -
            reaction_counter++;
        }
        buffer.append("\n");

        buffer.append("# Formulate the saturation constant array - \n");
        buffer.append("saturation_constant_array = zeros(NREACTIONS,NSPECIES);\n");
        reaction_name_iterator = reaction_name_list.iterator();
        reaction_counter = 1;
        while (reaction_name_iterator.hasNext()){

            // Get the reaction name -
            String reaction_name = (String) reaction_name_iterator.next();

            // is this a signal transduction reaction?
            if (model_tree.isThisASignalTransductionReaction(reaction_name) &&
                    !model_tree.isThisADegradationReaction(reaction_name)){

                // Get the reaction comment string -
                String comment_string = model_tree.buildReactionCommentStringForReactionWithName(reaction_name);

                // ok, we have a signal transduction reaction -
                // Get the reactants -
                Vector<VLCGSimpleSpeciesModel> speciesModels = model_tree.getReactantsForSignalTransductionReactionWithName(reaction_name);
                Iterator<VLCGSimpleSpeciesModel> species_iterator = speciesModels.iterator();
                while (species_iterator.hasNext()){

                    // model -
                    VLCGSimpleSpeciesModel model = species_iterator.next();

                    // Get the species_symbol -
                    String species_symbol = (String)model.getModelComponent(VLCGSimpleSpeciesModel.SPECIES_SYMBOL);

                    // Get the index of this symbol -
                    if (species_symbol.equalsIgnoreCase("[]") == false){

                        // lookup the index -
                        int species_index = species_symbol_vector.indexOf(species_symbol);

                        // write the record -
                        buffer.append("saturation_constant_array[");
                        buffer.append(reaction_counter);
                        buffer.append(",");
                        buffer.append(species_index+1);
                        buffer.append("] = 1.0;\t# ");
                        buffer.append(comment_string);
                        buffer.append("\t species: ");
                        buffer.append(species_symbol);
                        buffer.append("\n");
                    }
                }
            }

            // update -
            reaction_counter++;
        }

        buffer.append("\n");
        buffer.append("# Formulate control parameter array - \n");
        int number_of_control_terms = model_tree.calculateTheTotalNumberOfControlTerms();
        buffer.append("control_parameter_array = zeros(");
        buffer.append(number_of_control_terms);
        buffer.append(",2);\n");
        reaction_name_iterator = reaction_name_list.iterator();
        int control_index = 1;
        while (reaction_name_iterator.hasNext()) {

            // Get the reaction name -
            String reaction_name = (String) reaction_name_iterator.next();

            if (model_tree.isThisReactionRegulated(reaction_name)) {

                // Get the vector of transfer function wrappers -
                Vector<VLCGSimpleControlLogicModel> control_model_vector = model_tree.getControlModelListFromGRNModelTreeForReactionWithName(reaction_name);
                Iterator<VLCGSimpleControlLogicModel> control_iterator = control_model_vector.iterator();
                while (control_iterator.hasNext()) {

                    // Get the comment from tghe control model
                    VLCGSimpleControlLogicModel control_model = control_iterator.next();
                    String comment = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT);
                    String header_comment = model_tree.buildControlCommentStringForControlConnectionWithName((String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_NAME));
                    String control_type = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_TYPE);


                    if (control_type.contains("threshold")){

                        // write the gain line -
                        buffer.append("# ");
                        buffer.append(header_comment);
                        buffer.append("\n");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1] = 0.1;\t#\t");
                        buffer.append(control_index);
                        buffer.append(" Threshold: \t");
                        buffer.append(comment);
                        buffer.append("\n");

                        // write the order line -
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2] = 1.0;\t#\t");
                        buffer.append(control_index);
                        buffer.append(" Gain: \t");
                        buffer.append(comment);
                        buffer.append("\n\n");
                    }
                    else {

                        // write the gain line -
                        buffer.append("# ");
                        buffer.append(header_comment);
                        buffer.append("\n");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1] = 0.1;\t#\t");
                        buffer.append(control_index);
                        buffer.append(" Gain: \t");
                        buffer.append(comment);
                        buffer.append("\n");

                        // write the order line -
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2] = 1.0;\t#\t");
                        buffer.append(control_index);
                        buffer.append(" Order: \t");
                        buffer.append(comment);
                        buffer.append("\n\n");
                    }


                    // update counter -
                    control_index++;
                }
            }
        }

        //buffer.append("\n");
        buffer.append("# Set the maximum specific growth rate - \n");
        buffer.append("maximum_specific_growth_rate = 0.5;\n");
        buffer.append("dilution_selection_matrix = eye(NSPECIES);\n");
        buffer.append("dilution_selection_matrix[1:number_of_genes,1:number_of_genes] = 0.0;\n");

        buffer.append("\n");
        buffer.append("# ---------------------------- DO NOT EDIT BELOW THIS LINE -------------------------- #\n");
        buffer.append("data_dictionary = Dict();\n");
        buffer.append("data_dictionary[\"STOICHIOMETRIC_MATRIX\"] = S;\n");
        buffer.append("data_dictionary[\"RATE_CONSTANT_ARRAY\"] = rate_constant_array;\n");
        buffer.append("data_dictionary[\"SATURATION_CONSTANT_ARRAY\"] = saturation_constant_array;\n");
        buffer.append("data_dictionary[\"INITIAL_CONDITION_ARRAY\"] = initial_condition_array;\n");
        buffer.append("data_dictionary[\"TIME_CONSTANT_ARRAY\"] = time_constant_array;\n");
        buffer.append("data_dictionary[\"CONTROL_PARAMETER_ARRAY\"] = control_parameter_array;\n");
        buffer.append("data_dictionary[\"MAXIMUM_SPECIFIC_GROWTH_RATE\"] = maximum_specific_growth_rate;\n");
        buffer.append("data_dictionary[\"DILUTION_SELECTION_MATRIX\"] = dilution_selection_matrix;\n");
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
        buffer.append("EPSILON = 1.0e-3;\n");
        buffer.append("number_of_reactions = length(rate_vector);\n");
        buffer.append("control_vector = ones(number_of_reactions);\n");
        buffer.append("control_parameter_array = data_dictionary[\"CONTROL_PARAMETER_ARRAY\"];\n");
        buffer.append("\n");

        buffer.append("# Alias the species vector - \n");
        Vector<String> listOfSpecies = model_tree.getSpeciesSymbolsFromGRNModel();
        int number_of_species = listOfSpecies.size();
        for (int species_index = 0;species_index<number_of_species;species_index++){

            // Get the symbol -
            String species_symbol = listOfSpecies.get(species_index);

            // write the symbol =
            buffer.append(species_symbol);
            buffer.append(" = x[");
            buffer.append(species_index + 1);
            buffer.append("];\n");
        }
        buffer.append("\n");

        Vector<String> reaction_name_list = model_tree.getListOfReactionNamesFromGRNModelTree();
        Iterator reaction_name_iterator = reaction_name_list.iterator();
        int reaction_index = 1;
        int control_index = 1;
        while (reaction_name_iterator.hasNext()){

            // Get the reaction name -
            String reaction_name = (String)reaction_name_iterator.next();

            System.out.println("Checking - "+reaction_name);

            // is this reaction regulated?
            if (model_tree.isThisReactionRegulated(reaction_name)) {

                // ok, we have a regulation term for this reaction
                buffer.append("# ----------------------------------------------------------------------------------- #\n");
                buffer.append("transfer_function_vector = Float64[];\n");
                buffer.append("\n");

                // Get the vector of transfer function wrappers -
                Vector<VLCGSimpleControlLogicModel> control_model_vector = model_tree.getControlModelListFromGRNModelTreeForReactionWithName(reaction_name);
                Iterator<VLCGSimpleControlLogicModel> control_iterator = control_model_vector.iterator();
                while (control_iterator.hasNext()){

                    // Get the control model -
                    VLCGSimpleControlLogicModel control_model = control_iterator.next();

                    // Get the comment -
                    String comment = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT);
                    buffer.append("# ");
                    buffer.append(comment);
                    buffer.append("\n");


                    // Get the data from the model -
                    String actor = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_ACTOR);
                    String type = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_TYPE);

                    // Check the type -
                    if (type.equalsIgnoreCase("repression") || type.equalsIgnoreCase("inhibition")){

                        // write -

                        // check do we have a zero inhibitor?

                        buffer.append("if (");
                        buffer.append(actor);
                        buffer.append("<EPSILON);\n");
                        buffer.append("\tpush!(transfer_function_vector,0.0);\n");
                        buffer.append("else\n");
                        buffer.append("\tpush!(transfer_function_vector,1.0 - (control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2])/(1+");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]));\n");
                        buffer.append("end\n");
                        buffer.append("\n");
                    }
                    else if (type.equalsIgnoreCase("positive_threshold")) {

                        // encode 0 -> 1*gain if actor > threshold
                        buffer.append("if (");
                        buffer.append(actor);
                        buffer.append("> control_parameter_array[\n");
                        buffer.append(control_index);
                        buffer.append(",1])\n");
                        buffer.append("\tpush!(transfer_function_vector,");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]);\n");
                        buffer.append("else\n");
                        buffer.append("\tpush!(transfer_function_vector,0.0);\n");
                        buffer.append("end\n");
                        buffer.append("\n");
                    }
                    else if (type.equalsIgnoreCase("negative_threshold")) {

                        // encode gain -> 0 if actor > threshold
                        buffer.append("if (");
                        buffer.append(actor);
                        buffer.append("> control_parameter_array[\n");
                        buffer.append(control_index);
                        buffer.append(",1])\n");
                        buffer.append("\tpush!(transfer_function_vector,0.0);\n");
                        buffer.append("else\n");
                        buffer.append("\tpush!(transfer_function_vector,");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]);\n");
                        buffer.append("end\n");
                        buffer.append("\n");
                    }
                    else {

                        // write -
                        buffer.append("push!(transfer_function_vector,(control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2])/(1+");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]));\n");
                    }

                    // update control_index -
                    control_index++;
                }

                // integrate the transfer functions -
                buffer.append("control_vector[");
                buffer.append(reaction_index);
                buffer.append("] = mean(transfer_function_vector);\n");
                buffer.append("transfer_function_vector = 0;\n");
                buffer.append("# ----------------------------------------------------------------------------------- #\n");
                buffer.append("\n");
            }

            // update the counter -
            reaction_index++;
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
