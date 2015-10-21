package org.varnerlab.kwatee.grnmodel;

import org.varnerlab.kwatee.grnmodel.models.VLCGSimpleControlLogicModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
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
public class VLCGGRNModelTreeWrapper {

    // instance variables -
    private Document _model_tree = null;
    private XPathFactory _xpath_factory = XPathFactory.newInstance();
    private XPath _xpath = _xpath_factory.newXPath();

    public VLCGGRNModelTreeWrapper(Document document) {

        // grab the document -
        _model_tree = document;

        // init me -
        _init();
    }

    private void _init(){
    }



    public boolean isThisReactionRegulated(String reaction_name) throws Exception {

        // method variables -
        boolean return_flag = false;

        // ok, check - do we have a control statement with this reaction name as the target?
        String xpath_string = ".//control[@control_target=\""+reaction_name+"\"]";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        if (node_list.getLength()>0){
            return_flag = true;
        }

        // return -
        return return_flag;
    }

    public String getInitialAmountForSpeciesWithSymbol(String symbol) throws Exception {

        // Get the species from the model list -
        String xpath_string = ".//species[@id=\""+symbol+"\"]/@initial_amount";
        return _lookupPropertyValueFromTreeUsingXPath(xpath_string);
    }

    public int calculateTheTotalNumberOfControlTerms() throws Exception {

        // method variables -
        int number_of_control_terms = 0;

        String xpath_string = ".//control/@control_name";
        NodeList nodeList = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);
        number_of_control_terms = nodeList.getLength();

        // return -
        return number_of_control_terms;
    }


    public Vector<VLCGSimpleControlLogicModel> getControlModelListFromGRNModelTreeForReactionWithName(String reaction_name) throws Exception {

        // method variables -
        Vector<VLCGSimpleControlLogicModel> control_vector = new Vector<VLCGSimpleControlLogicModel>();

        // ok, check - do we have a control statement with this reaction name as the target?
        String xpath_string = ".//control[@control_target=\""+reaction_name+"\"]";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);
        int number_of_transfer_functions = node_list.getLength();
        for (int transfer_function_index = 0;transfer_function_index<number_of_transfer_functions;transfer_function_index++){

            // Get the node -
            Node control_node = node_list.item(transfer_function_index);

            // Get the data from this node -
            NamedNodeMap attribute_map = control_node.getAttributes();
            Node type_node = attribute_map.getNamedItem("control_type");
            Node actor_node = attribute_map.getNamedItem("control_actor");
            Node name_node = attribute_map.getNamedItem("control_name");

            // Create a comment -
            String comment = name_node.getNodeValue()+" target: "+reaction_name+" actor: "+actor_node.getNodeValue()+" type: "+type_node.getNodeValue();

            // Build the wrapper -
            VLCGSimpleControlLogicModel transfer_function_model = new VLCGSimpleControlLogicModel();
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_ACTOR,actor_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_TARGET,reaction_name);
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_TYPE,type_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_NAME,name_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT,comment);

            // add to the vector -
            control_vector.addElement(transfer_function_model);
        }

        return control_vector;
    }

    public Vector<String> getSpeciesSymbolsFromGRNModel() throws Exception {

        // method variables -
        Vector<String> species_vector = new Vector<String>();

        // Get the species from the model list -
        String xpath_string = ".//species/@id";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        // ok, so we need to grab the node values, and return the string symbols
        int number_of_nodes = node_list.getLength();
        for (int node_index = 0;node_index<number_of_nodes;node_index++){

            // Grab the node value -
            String node_value = node_list.item(node_index).getNodeValue();
            species_vector.addElement(node_value);
        }

        // return -
        return species_vector;
    }

    public Vector<String> getListOfReactionNamesFromGRNModelTree() throws Exception {

        // method variables -
        Vector<String> name_vector = new Vector<String>();

        // Get reaction names -
        String xpath_string = ".//reaction/@name";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        // ok, so we need to grab the node values, and return the string symbols
        int number_of_nodes = node_list.getLength();
        for (int node_index = 0;node_index<number_of_nodes;node_index++){

            // Grab the node value -
            String node_value = node_list.item(node_index).getNodeValue();

            if (name_vector.contains(node_value) == false){
                name_vector.addElement(node_value);
            }
        }

        // We also need to get all the gene expression reactions!
        String gene_expression_xpath_string = ".//gene_expression_reaction/@name";
        node_list = _lookupPropertyCollectionFromTreeUsingXPath(gene_expression_xpath_string);

        // ok, so we need to grab the node values, and return the string symbols
        number_of_nodes = node_list.getLength();
        for (int node_index = 0;node_index<number_of_nodes;node_index++){

            // Grab the node value -
            String node_value = node_list.item(node_index).getNodeValue();

            if (name_vector.contains(node_value) == false){
                name_vector.addElement(node_value);
            }
        }

        // We also need to get all the gene expression reactions!
        String translation_xpath_string = ".//translation_reaction/@name";
        node_list = _lookupPropertyCollectionFromTreeUsingXPath(translation_xpath_string);

        // ok, so we need to grab the node values, and return the string symbols
        number_of_nodes = node_list.getLength();
        for (int node_index = 0;node_index<number_of_nodes;node_index++){

            // Grab the node value -
            String node_value = node_list.item(node_index).getNodeValue();

            if (name_vector.contains(node_value) == false){
                name_vector.addElement(node_value);
            }
        }

        // return -
        return name_vector;
    }


    private NodeList _lookupPropertyCollectionFromTreeUsingXPath(String xpath_string) throws Exception {

        if (xpath_string == null) {
            throw new Exception("Null xpath in property lookup call.");
        }

        // Exceute the xpath -
        NodeList node_list = null;
        try {

            node_list = (NodeList) _xpath.evaluate(xpath_string, _model_tree, XPathConstants.NODESET);

        }
        catch (Exception error) {
            error.printStackTrace();
            System.out.println("ERROR: Property lookup failed. The following XPath "+xpath_string+" resulted in an error - "+error.toString());
        }

        // return -
        return node_list;
    }

    /**
     * Return the string value obtained from executing the XPath query passed in as an argument
     * @param String xpath_string
     * @return String - get property from uxml tree by executing string in strXPath
     */
    private String _lookupPropertyValueFromTreeUsingXPath(String xpath_string) throws Exception {

        if (xpath_string == null)
        {
            throw new Exception("ERROR: Null xpath in property lookup call.");
        }

        // Method attributes -
        String property_string = "";

        try {
            Node propNode = (Node) _xpath.evaluate(xpath_string, _model_tree, XPathConstants.NODE);
            property_string = propNode.getNodeValue();
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR: Property lookup failed. The following XPath "+xpath_string+" resulted in an error - "+error.toString());
        }

        return property_string;
    }
}
