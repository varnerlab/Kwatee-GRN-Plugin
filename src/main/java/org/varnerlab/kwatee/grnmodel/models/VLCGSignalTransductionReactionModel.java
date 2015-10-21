package org.varnerlab.kwatee.grnmodel.models;

import java.util.Hashtable;
import java.util.StringTokenizer;
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
 * Created by jeffreyvarner on 10/19/15.
 */
public class VLCGSignalTransductionReactionModel implements VLCGGRNModelComponent {

    // instance variables -
    private Hashtable<String,Object> _reaction_component_table = new Hashtable<String,Object>();

    // hastable keys -
    public static final String SIGNAL_TRANSDUCTION_REACTION_NAME = "reaction_name";
    public static final String SIGNAL_TRANSDUCTION_REACTION_REACTANTS = "REACTANTS";
    public static final String SIGNAL_TRANSDUCTION_REACTION_PRODUCTS = "PRODUCTS";
    public static final String SIGNAL_TRANSDUCTION_REACTION_REVERSE = "REVERSE";
    public static final String SIGNAL_TRANSDUCTION_REACTION_FORWARD = "FORWARD";
    public static final String SIGNAL_TRANSDUCTION_REACTION_REACTANT_VECTOR = "REACTANT_VECTOR";
    public static final String SIGNAL_TRANSDUCTION_REACTION_PRODUCT_VECTOR = "PRODUCT_VECTOR";
    public static final String SIGNAL_TRANSDUCTION_REACTION_RAW_STRING = "raw_rxn_string";

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

        System.out.println("key = "+key+" value = "+value);

        // store the reaction component -
        _reaction_component_table.put(key,value);
    }

    @Override
    public Object doExecute() throws Exception {

        // Create reactant and product vectors -
        Vector<VLCGSignalTransductionProteinModel> reactant_vector = new Vector<VLCGSignalTransductionProteinModel>();
        Vector<VLCGSignalTransductionProteinModel> product_vector = new Vector<VLCGSignalTransductionProteinModel>();

        // Parse -
        _parseString((String) getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_REACTANTS), reactant_vector, false);
        _parseString((String) getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_PRODUCTS), product_vector, true);

        // Cache the product and reactant vector -
        setModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_REACTANT_VECTOR,reactant_vector);
        setModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_PRODUCT_VECTOR,product_vector);

        // return -
        return null;
    }

    // private methods -
    private void _parseString(String frag,Vector<VLCGSignalTransductionProteinModel> vector,boolean isProduct) throws Exception {

        // Ok, this method contains the logic to cut up the reaction strings -

        // Cut around the +'s'
        StringTokenizer tokenizer=new StringTokenizer(frag,"+",false);
        while (tokenizer.hasMoreElements()) {
            // Get a data from the tokenizer -
            Object dataChunk=tokenizer.nextToken();

            // Create new symbol wrapper
            VLCGSignalTransductionProteinModel symbol = new VLCGSignalTransductionProteinModel();

            // Check to see if this dataChunk string contains a *
            if (((String)dataChunk).contains("*")) {
                // If I get here, then the string contains a stoichiometric coefficient

                // Cut around the *'s
                StringTokenizer tokenizerCoeff=new StringTokenizer((String)dataChunk,"*",false);
                int intCoeffCounter = 1;
                while (tokenizerCoeff.hasMoreElements()) {

                    Object dataCoeff = tokenizerCoeff.nextToken();

                    if (intCoeffCounter==1) {
                        if (isProduct){
                            symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT,dataCoeff);
                        }
                        else {
                            symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT, "-" + dataCoeff);
                        }

                        // Update the counter
                        intCoeffCounter++;
                    }
                    else if (intCoeffCounter==2) {
                        symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL,(String)dataCoeff);
                    }
                }
            }
            else {
                // If I get here, then no coefficient
                if (isProduct) {
                    // If this metabolite is in a product string, then coeff is positive
                    symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL,(String)dataChunk);
                    symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT,"1.0");
                }
                else {
                    // If this metabolite is in a reactant string, then coeff is negative
                    symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL,(String)dataChunk);
                    symbol.setModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT,"-1.0");
                }
            }

            // Add to symbol wrapper to the vector -
            vector.addElement(symbol);
        }
    }
}
