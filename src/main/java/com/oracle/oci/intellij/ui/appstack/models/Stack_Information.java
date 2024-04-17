package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

public class Stack_Information extends VariableGroup{
    private String  appstack_name ;
    private String appstack_description ;
    private Object appstack_compartment ;


    @PropertyOrder(1)
    @VariableMetaData(title="Stack's Compartment",description="The compartment which stack will be created in",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true)
    public Object getAppstack_compartment() {
        return appstack_compartment;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="Name",description="This is name of your stack ",type="string")
    public String getAppstack_name() {
        return appstack_name;
    }

    public void setAppstack_name(String appstack_name) {
        this.appstack_name = appstack_name;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Description",description="This is the description  of your stack ",type="textArea")
    public String getAppstack_description() {
        return appstack_description;
    }

    public void setAppstack_description(String description) {
        this.appstack_description = description;
    }

    public void setAppstack_compartment(Object appstack_compartment) {
        this.appstack_compartment = appstack_compartment;
    }
}
