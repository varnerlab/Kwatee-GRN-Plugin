package org.varnerlab.kwatee.grnmodel;

// imports -
import org.varnerlab.kwatee.foundation.VLCGInputHandler;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;
import org.varnerlab.kwatee.grnmodel.models.*;
import org.varnerlab.kwatee.grnmodel.parserdelegates.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

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

public class VLCGParseVarnerGRNFlatFile implements VLCGInputHandler {

    // instance variables -
    private VLCGTransformationPropertyTree _transformation_properties_tree = null;
    private final String _package_name_parser_delegate = "org.varnerlab.kwatee.grnmodel.parserdelegates";
    private Hashtable<Class,Vector<VLCGGRNModelComponent>> _model_component_table = new Hashtable();

    @Override
    public void setPropertiesTree(VLCGTransformationPropertyTree properties_tree) {

        if (properties_tree == null){
            return;
        }

        _transformation_properties_tree = properties_tree;
    }

    @Override
    public void loadResource(Object o) throws Exception {

        // Where is the file that I need to load?
        String resource_file_path = _transformation_properties_tree.lookupKwateeNetworkFilePath();
        if (resource_file_path != null){

            // ok, we have what appears to be a path, read the GRN file at this location -
            _readGRNFlatFile(resource_file_path);
        }
        else {
            throw new Exception("ERROR: Missing resource file path. Can't find GRN description to parse.");
        }
    }

    @Override
    public Object getResource(Object o) throws Exception {

        // Method variables -
        StringBuffer xml_buffer = new StringBuffer();
        DocumentBuilder document_builder = null;
        Document model_tree = null;

        // ok, extract the list of genes and other species -
        String gene_buffer = _addListOfGenesFromModelTableToModelTree();
        String mRNA_buffer = _addListOfMRNAsFromModelTableToModelTree();
        String protein_buffer = _addListOfProteinsFromModelTableToModelTree();

        // Formulate the list of reactions -
        String gene_expression_buffer = _addListOfGeneExpressionReactionsFromModelTableToModelTree();
        String translation_expression_buffer = _addListOfTranslationReactionsFromModelTableToModelTree();
        String signal_transduction_buffer = _addListOfSignalTransductionReactionsFromModelTableToModelTree();
        String mRNA_degradation_buffer = _addListOfMRNADegradationReactionsFromModelTableToModelTree();

        // Formulate list of control terms -
        String signal_transduction_control_buffer = _addListOfSignalTransductionControlTermsFromModelTableToModelTree();
        String genetic_control_buffer = _addListOfGeneticControlTermsFromModelTableToModelTree();

        // ok, we have parsered all the components -
        // Now, let's go through and generate the model tree -
        xml_buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n");
        xml_buffer.append("<GRNModel>\n");

        // species -
        xml_buffer.append("\t<listOfSpecies>\n");
        xml_buffer.append(gene_buffer);
        xml_buffer.append(mRNA_buffer);
        xml_buffer.append(protein_buffer);
        xml_buffer.append("\t</listOfSpecies>\n");

        // Reactions -
        xml_buffer.append("\t<listOfReactions>\n");
        xml_buffer.append("\t\t<listOfGeneExpressionReactions>\n");
        xml_buffer.append(gene_expression_buffer);
        xml_buffer.append("\t\t</listOfGeneExpressionReactions>\n");
        xml_buffer.append("\t\t<listOfTranslationReactions>\n");
        xml_buffer.append(translation_expression_buffer);
        xml_buffer.append("\t\t</listOfTranslationReactions>\n");
        xml_buffer.append("\t\t<listOfSignalTransductionReactions>\n");
        xml_buffer.append(signal_transduction_buffer);
        xml_buffer.append(mRNA_degradation_buffer);
        xml_buffer.append("\t\t</listOfSignalTransductionReactions>\n");
        xml_buffer.append("\t</listOfReactions>\n");

        // Control terms -
        xml_buffer.append("\t<listOfControlTerms>\n");

        xml_buffer.append("\t\t<listOfGeneticControlTerms>\n");
        xml_buffer.append(genetic_control_buffer);
        xml_buffer.append("\t\t</listOfGeneticControlTerms>\n");
        xml_buffer.append("\t\t<listOfSignalTransductionControlTerms>\n");
        xml_buffer.append(signal_transduction_control_buffer);
        xml_buffer.append("\t\t</listOfSignalTransductionControlTerms>\n");
        xml_buffer.append("\t</listOfControlTerms>\n");
        xml_buffer.append("</GRNModel>\n");

        // Convert the string buffer into an XML Document object -
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        document_builder = factory.newDocumentBuilder();
        model_tree = document_builder.parse(new InputSource(new StringReader(xml_buffer.toString())));

        System.out.println("tree - "+xml_buffer.toString());

        // return the wrapped model_tree -
        VLCGGRNModelTreeWrapper model_wrapper = new VLCGGRNModelTreeWrapper(model_tree);
        return model_wrapper;
    }

    // private helper methods -
    private String _addListOfSignalTransductionControlTermsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the translation reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGSignalTransductionControlParserDelegate";
        Vector<VLCGGRNModelComponent> control_vector = _model_component_table.get(Class.forName(class_name_key));
        Iterator<VLCGGRNModelComponent> control_iterator = control_vector.iterator();
        while (control_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = control_iterator.next();

            // Get data from the model component -
            String control_name = (String)model_component.getModelComponent(VLCGSignalTransductionControlModel.SIGNAL_TRANSDUCTION_CONTROL_NAME);
            String control_type = (String)model_component.getModelComponent(VLCGSignalTransductionControlModel.SIGNAL_TRANSDUCTION_CONTROL_TYPE);
            String control_actor = (String)model_component.getModelComponent(VLCGSignalTransductionControlModel.SIGNAL_TRANSDUCTION_CONTROL_ACTOR);
            String control_target = (String)model_component.getModelComponent(VLCGSignalTransductionControlModel.SIGNAL_TRANSDUCTION_CONTROL_TARGET);

            // Write the line -
            buffer.append("\t\t\t");
            buffer.append("<control control_name=\"");
            buffer.append(control_name);
            buffer.append("\" control_actor=\"");
            buffer.append(control_actor);
            buffer.append("\" control_target=\"");
            buffer.append(control_target);
            buffer.append("\" control_type=\"");
            buffer.append(control_type);
            buffer.append("\" />\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfGeneticControlTermsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the translation reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGGeneExpressionControlParserDelegate";
        Vector<VLCGGRNModelComponent> control_vector = _model_component_table.get(Class.forName(class_name_key));
        Iterator<VLCGGRNModelComponent> control_iterator = control_vector.iterator();
        while (control_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = control_iterator.next();

            // Get data from the model component -
            String control_name = (String)model_component.getModelComponent(VLCGGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_NAME);
            String control_type = (String)model_component.getModelComponent(VLCGGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TYPE);
            String control_actor = (String)model_component.getModelComponent(VLCGGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_ACTOR);
            String control_target = (String)model_component.getModelComponent(VLCGGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TARGET);

            // Write the line -
            buffer.append("\t\t\t");
            buffer.append("<control control_name=\"");
            buffer.append(control_name);
            buffer.append("\" control_actor=\"");
            buffer.append(control_actor);
            buffer.append("\" control_target=\"");
            buffer.append(control_target);
            buffer.append("\" control_type=\"");
            buffer.append(control_type);
            buffer.append("\" />\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfMRNADegradationReactionsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the mRNAs -
        String class_name_key = _package_name_parser_delegate + ".VLCGTranslationParserDelegate";
        Vector<VLCGGRNModelComponent> gene_vector = _model_component_table.get(Class.forName(class_name_key));

        Iterator<VLCGGRNModelComponent> gene_iterator = gene_vector.iterator();
        while (gene_iterator.hasNext()){

            // Get the model component -
            VLCGGRNModelComponent model_component = gene_iterator.next();

            // grab the symbol -
            String mrna_symbol = (String)model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_MRNA_SYMBOL);
            String raw_reaction_string = "degradation_"+mrna_symbol+": "+mrna_symbol+" = []";

            // write the buffer line -
            buffer.append("\t\t\t");
            buffer.append("<reaction name=\"degradation_");
            buffer.append(mrna_symbol);
            buffer.append("\" raw_reaction_string=\"");
            buffer.append(raw_reaction_string);
            buffer.append("\">\n");

            buffer.append("\t\t\t\t");
            buffer.append("<listOfReactants>\n");

            buffer.append("\t\t\t\t\t");
            buffer.append("<speciesReference species=\"");
            buffer.append(mrna_symbol);
            buffer.append("\" stoichiometric_coefficient=\"-1.0\" />\n");
            buffer.append("\t\t\t\t");
            buffer.append("</listOfReactants>\n");


            buffer.append("\t\t\t\t");
            buffer.append("<listOfProducts>\n");

            // write into the buffer -
            buffer.append("\t\t\t\t\t");
            buffer.append("<speciesReference species=\"[]");
            buffer.append("\" stoichiometric_coefficient=\"1.0\" />\n");

            buffer.append("\t\t\t\t");
            buffer.append("</listOfProducts>\n");

            buffer.append("\t\t\t</reaction>\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfSignalTransductionReactionsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the translation reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGSignalTransductionParserDelegate";
        Vector<VLCGGRNModelComponent> signal_transduction_vector = _model_component_table.get(Class.forName(class_name_key));
        Iterator<VLCGGRNModelComponent> signal_transduction_iterator = signal_transduction_vector.iterator();
        while (signal_transduction_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = signal_transduction_iterator.next();

            // Get the reaction data -
            String reaction_name = (String)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_NAME);
            String reverse_flag = (String)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_REVERSE);
            String raw_string = (String)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_RAW_STRING);
            Vector<VLCGSignalTransductionProteinModel> reactant_model_vector = (Vector)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_REACTANT_VECTOR);
            Vector<VLCGSignalTransductionProteinModel> product_model_vector = (Vector)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_PRODUCT_VECTOR);

            // write the buffer line -
            buffer.append("\t\t\t");
            buffer.append("<reaction name=\"");
            buffer.append(reaction_name);
            buffer.append("\" raw_reaction_string=\"");
            buffer.append(raw_string);
            buffer.append("\">\n");
            buffer.append("\t\t\t\t");
            buffer.append("<listOfReactants>\n");

            Iterator<VLCGSignalTransductionProteinModel> reactant_iterator = reactant_model_vector.iterator();
            while (reactant_iterator.hasNext()){

                VLCGSignalTransductionProteinModel model = reactant_iterator.next();
                String symbol = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                String coefficient = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT);

                // write into the buffer -
                buffer.append("\t\t\t\t\t");
                buffer.append("<speciesReference species=\"");
                buffer.append(symbol);
                buffer.append("\" stoichiometric_coefficient=\"");
                buffer.append(coefficient);
                buffer.append("\" />\n");
            }

            buffer.append("\t\t\t\t");
            buffer.append("</listOfReactants>\n");


            buffer.append("\t\t\t\t");
            buffer.append("<listOfProducts>\n");

            Iterator<VLCGSignalTransductionProteinModel> product_iterator = product_model_vector.iterator();
            while (product_iterator.hasNext()){

                VLCGSignalTransductionProteinModel model = product_iterator.next();
                String symbol = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                String coefficient = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT);

                // write into the buffer -
                buffer.append("\t\t\t\t\t");
                buffer.append("<speciesReference species=\"");
                buffer.append(symbol);
                buffer.append("\" stoichiometric_coefficient=\"");
                buffer.append(coefficient);
                buffer.append("\" />\n");
            }

            buffer.append("\t\t\t\t");
            buffer.append("</listOfProducts>\n");

            buffer.append("\t\t\t</reaction>\n");

            // if we have a reverse reaction, add it ...
            if (reverse_flag.equalsIgnoreCase("-inf") == true){

                // write the buffer line -
                buffer.append("\t\t\t");
                buffer.append("<reaction name=\"");
                buffer.append(reaction_name+"_reverse");
                buffer.append("\" raw_reaction_string=\"");

                // we need to redo the raw string ..
                StringBuffer local_reaction_string_buffer = new StringBuffer();
                StringTokenizer tokenizer = new StringTokenizer(raw_string," ");
                String name = "";
                String reactants = "";
                String products = "";
                String equals_sign = "";
                int counter = 1;
                while (tokenizer.hasMoreTokens()){

                    String token = tokenizer.nextToken();

                    if (counter == 1){
                        name = token;
                    }
                    else if (counter == 2){
                        reactants = token;
                    }
                    else if (counter == 3){
                        equals_sign = token;
                    }
                    else if (counter == 4){
                        products = token;
                    }

                    // update the counter -
                    counter++;
                }

                local_reaction_string_buffer.append(name);
                local_reaction_string_buffer.append(" ");
                local_reaction_string_buffer.append(products);
                local_reaction_string_buffer.append(" ");
                local_reaction_string_buffer.append(equals_sign);
                local_reaction_string_buffer.append(" ");
                local_reaction_string_buffer.append(reactants);
                local_reaction_string_buffer.append(" ");
                local_reaction_string_buffer.append("(reverse)");

                buffer.append(local_reaction_string_buffer.toString());
                buffer.append("\">\n");

                buffer.append("\t\t\t\t");
                buffer.append("<listOfReactants>\n");

                Iterator<VLCGSignalTransductionProteinModel> reverse_product_iterator = product_model_vector.iterator();
                while (reverse_product_iterator.hasNext()){

                    VLCGSignalTransductionProteinModel model = reverse_product_iterator.next();
                    String symbol = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                    String coefficient = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT);

                    // write into the buffer -
                    buffer.append("\t\t\t\t\t");
                    buffer.append("<speciesReference species=\"");
                    buffer.append(symbol);
                    buffer.append("\" stoichiometric_coefficient=\"");
                    buffer.append(-1.0*Double.parseDouble(coefficient));
                    buffer.append("\" />\n");
                }

                buffer.append("\t\t\t\t");
                buffer.append("</listOfReactants>\n");


                buffer.append("\t\t\t\t");
                buffer.append("<listOfProducts>\n");

                Iterator<VLCGSignalTransductionProteinModel> reverse_reactant_iterator = reactant_model_vector.iterator();
                while (reverse_reactant_iterator.hasNext()){

                    VLCGSignalTransductionProteinModel model = reverse_reactant_iterator.next();
                    String symbol = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                    String coefficient = (String)model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_COEFFICIENT);

                    // write into the buffer -
                    buffer.append("\t\t\t\t\t");
                    buffer.append("<speciesReference species=\"");
                    buffer.append(symbol);
                    buffer.append("\" stoichiometric_coefficient=\"");
                    buffer.append(-1.0*Double.parseDouble(coefficient));
                    buffer.append("\" />\n");
                }


                buffer.append("\t\t\t\t");
                buffer.append("</listOfProducts>\n");
                buffer.append("\t\t\t</reaction>\n");
            }
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfTranslationReactionsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the translation reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGTranslationParserDelegate";
        Vector<VLCGGRNModelComponent> translation_vector = _model_component_table.get(Class.forName(class_name_key));
        Iterator<VLCGGRNModelComponent> translation_iterator = translation_vector.iterator();
        while (translation_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = translation_iterator.next();

            // write the buffer line -
            buffer.append("\t\t\t");
            buffer.append("<translation_reaction mrna_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_MRNA_SYMBOL));
            buffer.append("\" protein_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_PROTEIN_SYMBOL));
            buffer.append("\" ribosome_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_RIBOSOME_SYMBOL));
            buffer.append("\" name=\"");
            buffer.append(model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_REACTION_NAME));
            buffer.append("\" raw_reaction_string=\"");
            buffer.append(model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_REACTION_RAW_STRING));
            buffer.append("\"/>\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfGeneExpressionReactionsFromModelTableToModelTree() throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the proteins -
        String class_name_key = _package_name_parser_delegate + ".VLCGGeneExpressionParserDelegate";
        Vector<VLCGGRNModelComponent> gene_expression_vector = _model_component_table.get(Class.forName(class_name_key));
        Iterator<VLCGGRNModelComponent> gene_expression_iterator = gene_expression_vector.iterator();
        while (gene_expression_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = gene_expression_iterator.next();

            // write the buffer line -
            buffer.append("\t\t\t");
            buffer.append("<gene_expression_reaction gene_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_GENE_SYMBOL));
            buffer.append("\" mrna_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_MRNA_SYMBOL));
            buffer.append("\" rnap_symbol=\"");
            buffer.append(model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_RNA_POLYMERASE_SYMBOL));
            buffer.append("\" name=\"");
            buffer.append(model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_REACTION_NAME));
            buffer.append("\" raw_reaction_string=\"");
            buffer.append(model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_REACTION_RAW_STRING));
            buffer.append("\"/>\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfProteinsFromModelTableToModelTree() throws Exception {

        // Method variables -
        Vector<String> symbol_vector = new Vector<String>();
        StringBuffer buffer = new StringBuffer();

        // Get the proteins -
        String class_name_key = _package_name_parser_delegate + ".VLCGSignalTransductionParserDelegate";
        Vector<VLCGGRNModelComponent> protein_vector = _model_component_table.get(Class.forName(class_name_key));

        Iterator<VLCGGRNModelComponent> protein_iterator = protein_vector.iterator();
        while (protein_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = protein_iterator.next();

            // go through the products and reactants -
            // Reactants -
            Vector<VLCGSignalTransductionProteinModel> reactant_vector = (Vector)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_REACTANT_VECTOR);
            Iterator<VLCGSignalTransductionProteinModel> reactant_iterator = reactant_vector.iterator();
            while (reactant_iterator.hasNext()){

                // Get the protein model -
                VLCGSignalTransductionProteinModel reactant_product_model = reactant_iterator.next();

                // do we already have this symbol?
                String symbol = (String)reactant_product_model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                if (symbol_vector.contains(symbol) == false && symbol.equalsIgnoreCase("[]") == false){
                    symbol_vector.addElement(symbol);
                }
            }

            // Products -
            Vector<VLCGSignalTransductionProteinModel> product_vector = (Vector)model_component.getModelComponent(VLCGSignalTransductionReactionModel.SIGNAL_TRANSDUCTION_REACTION_PRODUCT_VECTOR);
            Iterator<VLCGSignalTransductionProteinModel> product_iterator = product_vector.iterator();
            while (product_iterator.hasNext()){

                // Get the protein model -
                VLCGSignalTransductionProteinModel reactant_product_model = product_iterator.next();

                // do we already have this symbol?
                String symbol = (String)reactant_product_model.getModelComponent(VLCGSignalTransductionProteinModel.SIGNAL_TRANSDUCTION_PROTEIN_SYMBOL);
                if (symbol_vector.contains(symbol) == false && symbol.equalsIgnoreCase("[]") == false){
                    symbol_vector.addElement(symbol);
                }
            }
        }

        // ok, so when I get here, I have all the symbols - build the xml records -
        Iterator<String> symbol_iterator = symbol_vector.iterator();
        Vector<String> tmp_species_vector = new Vector<String>();
        while (symbol_iterator.hasNext()){

            // Symbol -
            String protein_symbol = symbol_iterator.next();

            if (tmp_species_vector.contains(protein_symbol) == false){

                // build the record -
                buffer.append("\t\t<species id=\"");
                buffer.append(protein_symbol);
                buffer.append("\" species_type=\"PROTEIN\" initial_amount=\"0.0\"/>\n");

                // add -
                tmp_species_vector.addElement(protein_symbol);
            }
        }

        // We need to get the list of proteins that are coming from translation -
        String translation_class_name_key = _package_name_parser_delegate + ".VLCGTranslationParserDelegate";
        Vector<VLCGGRNModelComponent> translation_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        Iterator<VLCGGRNModelComponent> translation_iterator = translation_vector.iterator();
        while (translation_iterator.hasNext()) {

            // Get the model component -
            VLCGGRNModelComponent model_component = translation_iterator.next();

            // Get the symbol -
            String translation_product_symbol = (String)model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_PROTEIN_SYMBOL);


            if (tmp_species_vector.contains(translation_product_symbol) == false){

                // build the record -
                buffer.append("\t\t<species id=\"");
                buffer.append(translation_product_symbol);
                buffer.append("\" species_type=\"PROTEIN\" initial_amount=\"0.0\"/>\n");

                // add -
                tmp_species_vector.addElement(translation_product_symbol);
            }
        }

        // return -
        return buffer.toString();
    }


    private String _addListOfMRNAsFromModelTableToModelTree() throws Exception {

        // Method variables -
        Vector<String> symbol_vector = new Vector<String>();
        StringBuffer buffer = new StringBuffer();

        // Get the mRNAs -
        String class_name_key = _package_name_parser_delegate + ".VLCGTranslationParserDelegate";
        Vector<VLCGGRNModelComponent> gene_vector = _model_component_table.get(Class.forName(class_name_key));

        Iterator<VLCGGRNModelComponent> gene_iterator = gene_vector.iterator();
        while (gene_iterator.hasNext()){

            // Get the model component -
            VLCGGRNModelComponent model_component = gene_iterator.next();

            System.out.println(model_component);

            // grab the symbol -
            String gene_symbol = (String)model_component.getModelComponent(VLCGTranslationReactionModel.TRANSLATION_MRNA_SYMBOL);

            // create the buffer entry -
            buffer.append("\t\t<species id=\"");
            buffer.append(gene_symbol);
            buffer.append("\" species_type=\"MRNA\" initial_amount=\"0.0\"/>\n");
        }

        // return -
        return buffer.toString();
    }

    private String _addListOfGenesFromModelTableToModelTree() throws Exception {

        // Method variables -
        Vector<String> symbol_vector = new Vector<String>();
        StringBuffer buffer = new StringBuffer();

        // Get the genes -
        String class_name_key = _package_name_parser_delegate + ".VLCGGeneExpressionParserDelegate";
        Vector<VLCGGRNModelComponent> gene_vector = _model_component_table.get(Class.forName(class_name_key));

        Iterator<VLCGGRNModelComponent> gene_iterator = gene_vector.iterator();
        while (gene_iterator.hasNext()){

            // Get the model component -
            VLCGGRNModelComponent model_component = gene_iterator.next();

            System.out.println(model_component);

            // grab the symbol -
            String gene_symbol = (String)model_component.getModelComponent(VLCGGeneExpressionReactionModel.GENE_EXPRESSION_GENE_SYMBOL);

            // create the buffer entry -
            buffer.append("\t\t<species id=\"");
            buffer.append(gene_symbol);
            buffer.append("\" species_type=\"GENE\" initial_amount=\"1.0\"/>\n");
        }

        // return -
        return buffer.toString();
    }

    private void _readGRNFlatFile(String fileName) throws Exception {

        // method allocation -
        VLCGParserHandlerDelegate parser_delegate = null;

        // check -
        if (fileName == null){
            throw new Exception("ERROR: Missing or null requirements for parsing the GRN flat file.");
        }

        BufferedReader inReader = new BufferedReader(new FileReader(fileName));
        inReader.mark(0);
        String dataRecord = null;
        Vector<VLCGGRNModelComponent> model_components_vector = null;
        while ((dataRecord = inReader.readLine()) != null) {

            int whitespace = dataRecord.length();

            // Need to check to make sure I have do not have a comment
            if (!dataRecord.contains("//") && whitespace != 0) {

                // Does this record start with a #pragma?
                if (dataRecord.contains("#pragma") == true){

                    // ok, this is a handler directive -
                    String[] tmp = dataRecord.split(" ");
                    String handler_class_name = tmp[tmp.length - 1];

                    // Create fully quaified class name -
                    String fully_qualified_handler_name = _package_name_parser_delegate+"."+handler_class_name;

                    // Create the handler -
                    parser_delegate = (VLCGParserHandlerDelegate)Class.forName(fully_qualified_handler_name).newInstance();

                    // Create a new vector -
                    model_components_vector = new Vector();
                }
                else {

                    // this is a "regular" line in the file -
                    // Do we have a parser handler?
                    if (parser_delegate == null){
                        throw new Exception("ERROR: The parser delegate is null. Check your #pragma parser directives.");
                    }

                    // If we get here, we have a parser delegate ...
                    VLCGGRNModelComponent modelComponent = (VLCGGRNModelComponent)parser_delegate.parseLine(dataRecord);
                    modelComponent.doExecute();

                    // add this component to the vector -
                    model_components_vector.addElement(modelComponent);

                    // Add this vector to the hashtable -
                    _model_component_table.put(parser_delegate.getClass(),model_components_vector);
                }
            }
        }

        // close -
        inReader.close();
    }
}
