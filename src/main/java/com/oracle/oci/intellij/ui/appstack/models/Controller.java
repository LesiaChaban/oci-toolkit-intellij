package com.oracle.oci.intellij.ui.appstack.models;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.http.client.internal.ExplicitlySetBmcModel;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.resourcemanager.model.Stack;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.responses.ListStacksResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.CommandStack;
import com.oracle.oci.intellij.ui.appstack.actions.CustomWizardStep;
import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.SetCommand;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.util.LogHandler;
import jnr.ffi.Struct;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Controller {
    Map<String, PropertyDescriptor> descriptorsState;
    Map <String, CustomWizardStep.VarPanel> varPanels = new LinkedHashMap<>() ;
    Map<String, VariableGroup> variableGroups ;
    private static Controller instance ;
    Map<String, List<Compartment>> cachedCompartmentList = new LinkedHashMap<>();




    public Map<String, PropertyDescriptor> getDescriptorsState() {
        return descriptorsState;
    }
    public void addVariablePanel (CustomWizardStep.VarPanel varPanel){
        varPanels.put(varPanel.getPd().getName(),varPanel);
    }

    public void setDescriptorsState(LinkedHashMap<String, PropertyDescriptor> descriptorsState) {
        this.descriptorsState = descriptorsState;
    }

    public Map<String, List<Compartment>> getCachedCompartmentList() {
        return cachedCompartmentList;
    }

    public void setCachedCompartmentList(Map<String, List<Compartment>> cachedCompartmentList) {
        this.cachedCompartmentList = cachedCompartmentList;
    }

    public JComponent getComponentByName(String pdName){
        CustomWizardStep.VarPanel varPanel = varPanels.get(pdName);
        return varPanel.getMainComponent();
    }
    public JComponent getErrorLabelByName(String pdName){
        CustomWizardStep.VarPanel varPanel = varPanels.get(pdName);
        return varPanel.getErrorLabel();
    }
    public VariableGroup getVarGroupByName(String pdName){
        CustomWizardStep.VarPanel varPanel = varPanels.get(pdName);
        return varPanel.getVariableGroup();
    }

    public CustomWizardStep.VarPanel getVarPanelByName(String pdName){
        return varPanels.get(pdName);
    }

    public Map<String, VariableGroup> getVariableGroups() {
        return variableGroups;
    }

    public void setVariableGroups(Map<String, VariableGroup> variableGroups) {
        this.variableGroups = variableGroups;
    }

    public static Controller getInstance() {
        if (instance == null){
            instance = new Controller();
        }
        return instance;
    }

    public PropertyDescriptor getPdByName(String pdName){
        return descriptorsState.get(pdName);
    }



    public void updateDependencies(String pdName, VariableGroup varGroup){
        PropertyDescriptor pd = descriptorsState.get(pdName);
        List<String> dependencies = Utils.depondsOn.get(pd.getName());
        if (dependencies != null) {
            for (String dependent : dependencies) {
                CustomWizardStep.VarPanel varPanel = varPanels.get(dependent);
                ComboBox jComboBox = (ComboBox) varPanel.getMainComponent();
                if (jComboBox == null) continue;
                jComboBox.removeAllItems();
                jComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"Loading..."}));
                jComboBox.setEnabled(false);
                PropertyDescriptor dependentPd = descriptorsState.get(dependent);
                loadComboBoxValues(dependentPd,varPanel.getVariableGroup(),jComboBox);
            }
        }
    }

    public void loadComboBoxValues(PropertyDescriptor pd, VariableGroup varGroup, ComboBox comboBox) {
        new SwingWorker<List, Void>() {
            @Override
            protected List doInBackground() throws Exception {
                return getSuggestedValues(pd,varGroup);
            }

            @Override
            protected void done() {
                List<ExplicitlySetBmcModel> suggestedValues=new ArrayList<>();
                try {
                    suggestedValues = (List<ExplicitlySetBmcModel>) get();
                } catch (InterruptedException | ExecutionException e) {
                    UIUtil.fireNotification(NotificationType.WARNING, "Resource not found: \n"+e.getMessage(), null);
                    comboBox.removeAllItems();
                    comboBox.setEnabled(true);
                    String errorMessage = "There was an error retrieving options";
                    comboBox.setModel(new DefaultComboBoxModel(new String[]{errorMessage}));
                    setValue(null,varGroup,pd,false);
                    return;
                }
                comboBox.removeAllItems();
                comboBox.setEnabled(true);


                if (suggestedValues != null) {
                    for (ExplicitlySetBmcModel enumValue : suggestedValues) {
                        comboBox.addItem(enumValue);
                    }
                    if (!suggestedValues.isEmpty()){
                        comboBox.setSelectedItem(suggestedValues.get(0));
                        setValue(suggestedValues.get(0),varGroup,pd,false);
                    }
                }
            }
        }.execute();



    }
    public void updateVisibility(String pdName,VariableGroup variableGroup){
        PropertyDescriptor pd = descriptorsState.get(pdName);
        List<String> dependencies = Utils.visibility.get(pd.getName());
        if (dependencies != null) {

            for (String dependency : dependencies) {
                CustomWizardStep.VarPanel varPanel = varPanels.get(dependency);
                JComponent dependencyComponent  = varPanel.getMainComponent();
                if (dependencyComponent == null) continue;

                PropertyDescriptor dependentPd = descriptorsState.get(dependency);
                boolean isVisible = isVisible((String) dependentPd.getValue("visible"));
                varPanel.setVisible(isVisible);

                if (dependencyComponent instanceof JPanel){
                    JPanel dependencyComponentP = (JPanel) dependencyComponent;
                    Component[] components = dependencyComponentP.getComponents();
                    for (Component component:
                            components) {
                        if (component instanceof JButton){
                            component.setEnabled(isVisible);
                        }
                    }
                    continue;
                }
            }
        }
    }
    public boolean isVisible(String rule) {
        try {
            if (rule == null || rule.isEmpty()){
                return true;
            }
            if (rule.startsWith("not(")){
                return !isVisible(rule.substring(4,rule.length()-1));
            }
            if (rule.startsWith("and(")){
                return evaluateAnd(rule.substring(4, rule.lastIndexOf(')')));
            }
            if (rule.startsWith("eq(")){
                String[] parts = rule.substring(3,rule.length()-1).split(",");
                String variable;
                String value = parts[1].trim().replaceAll("'","");
                VariableGroup variableGroup = getVarGroupByName(parts[0]);
                PropertyDescriptor pd = descriptorsState.get(parts[0]);
                Enum varValue = (Enum) pd.getReadMethod().invoke(variableGroup);

                return varValue.toString().equals(value);
            }
            VariableGroup variableGroup = getVarGroupByName(rule.trim());
            PropertyDescriptor pd = descriptorsState.get(rule.trim());
            boolean varValue = false;

            varValue = (boolean) pd.getReadMethod().invoke(variableGroup);
            return varValue;

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private  boolean evaluateAnd(String rule) {
        int parenCount = 0;
        StringBuilder part = new StringBuilder();
        List<String> parts = new ArrayList<>();

        for (char c : rule.toCharArray()) {
            if (c == '(') {
                parenCount++;
            } else if (c == ')') {
                parenCount--;
            }

            if (c == ',' && parenCount == 0) {
                parts.add(part.toString());
                part = new StringBuilder();
            } else {
                part.append(c);
            }
        }
        parts.add(part.toString()); // Add the last part

        for (String p : parts) {
            if (!isVisible(p.trim())) {
                return false;
            }
        }
        return true;
    }
    public boolean doValidate(WizardStep wizardStep){
        CustomWizardStep cWizardStep = (CustomWizardStep)wizardStep;
        JComponent errorComponent = null;

        for (CustomWizardStep.VarPanel varPanel:
                cWizardStep.getVarPanels()) {
            PropertyDescriptor pd = varPanel.getPd();


            if (varPanel.isVisible() && (boolean)pd.getValue("required")){

                VariableGroup varGroup = getVarGroupByName(pd.getName());
                Object value = null;
                try {
                    value  = pd.getReadMethod().invoke(varGroup);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                if (!focusValidation(value,pd)){
                    errorComponent = getComponentByName(pd.getName());
                    errorComponent.grabFocus();
                    errorComponent.requestFocusInWindow();
                    return false;
                }

            }
        }

        return true;
    }

    boolean focusValidation(Object value,PropertyDescriptor pd){
        try {
            Validator.doValidate(pd,value,null);
            handleValidated(pd);
            return true;
        } catch (PropertyVetoException ex) {
            handleError(pd,ex.getMessage());
            return false;
        }
    }

    public VariableGroup getVariableGroup(PropertyDescriptor pd) {
        String className = pd.getReadMethod().getDeclaringClass().getSimpleName();

        return variableGroups.get(className);
    }



    public List<? extends ExplicitlySetBmcModel> getSuggestedValues(PropertyDescriptor pd, VariableGroup varGroup) {
        String varType = (String) pd.getValue("type");
        try {
            return Utils.getSuggestedValuesOf(varType).apply(pd, (LinkedHashMap<String, PropertyDescriptor>) descriptorsState,varGroup);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void setValue(Object newValue, VariableGroup variableGroup,PropertyDescriptor pd,boolean showError ){
        try {
            SetCommand setCommand = new SetCommand(variableGroup,pd,newValue);
            SetCommand.SetCommandResult result = (SetCommand.SetCommandResult) setCommand.execute();
            System.out.println(result);
            handleValidated(pd);

        }catch (InvocationTargetException ex){
            if (ex.getCause() instanceof PropertyVetoException) {
                if (showError)
                    handleError(pd,ex.getCause().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(Object value, VariableGroup variableGroup,PropertyDescriptor pd  ){
        setValue(value,variableGroup,pd,true);
    }


    public void handleValidated(PropertyDescriptor pd) {
        CustomWizardStep.VarPanel varPanel = getVarPanelByName(pd.getName());

        if (varPanel != null ){
            JComponent inputComponent = varPanel.getInputComponent();
            JLabel errorLabel = varPanel.getErrorLabel();
            if (pd.getValue("type").equals("textArea"))
                inputComponent.setBorder(UIManager.getBorder("TextArea.border")); // Reset to default border
            else
                inputComponent.setBorder(UIManager.getBorder("TextField.border")); // Reset to default border

            errorLabel.setText("");
        }

    }

    public void handleError(PropertyDescriptor pd,String errorMessage ){
        CustomWizardStep.VarPanel varPanel = getVarPanelByName(pd.getName());
        if (varPanel != null){
            JComponent component = varPanel.getMainComponent();
            JComponent inputComponent = varPanel.getInputComponent();
            JLabel errorLabel = varPanel.getErrorLabel();

            inputComponent.setBorder(BorderFactory.createLineBorder(JBColor.pink,3,true));
            errorLabel.setText(errorMessage);
            if (pd.getValue("errorMessage") != null && !((String)pd.getValue("errorMessage")).isEmpty())
                inputComponent.setToolTipText("Field should be : "+pd.getValue("errorMessage"));
        }
    }
    public Object getValue(VariableGroup variableGroup,PropertyDescriptor pd){
        try {
            return pd.getReadMethod().invoke(variableGroup);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public PropertyDescriptor[] getSortedProertyDescriptorsByVarGroup(VariableGroup varGroup) throws IntrospectionException {
        Class<? extends VariableGroup> varGroupClazz = varGroup.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(varGroupClazz);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

        Arrays.sort(propertyDescriptors, Comparator.comparingInt(pd -> {
            PropertyOrder annotation = pd.getReadMethod().getAnnotation(PropertyOrder.class);
            return (annotation != null) ? annotation.value() : Integer.MAX_VALUE;
        }));
        return propertyDescriptors;
    }

    public void initApplicationNames() {

        Executors.newSingleThreadExecutor().submit(()->{
            List<StackSummary> appStackList;
            List<String> appNames =new ArrayList<>() ;
            try {

                // I need to get the list of summary stacks
                CommandStack commandStack = new CommandStack();
                General_Configuration generalConfiguration=(General_Configuration) variableGroups.get("General_Configuration") ;
                String compartmentId = ((Compartment)generalConfiguration.getCompartment_id()).getId();
                ListStackCommand command = new ListStackCommand(OracleCloudAccount.getInstance().getResourceManagerClientProxy(), compartmentId);
                ListStackCommand.ListStackResult result =(ListStackCommand.ListStackResult) commandStack.execute(command);
                if (!result.isError()){
                    appStackList = result.getStacks();
                }
                else
                {
                    throw new AbstractBasicCommand.CommandFailedException("Failed refreshing list of stacks");
                }
            } catch (Exception exception) {
                appStackList = null;
                UIUtil.fireNotification(NotificationType.ERROR, exception.getMessage(), null);
                LogHandler.error(exception.getMessage(), exception);
            }
            //then get Stack of each summary stack
            for (StackSummary stackSummary: appStackList){
                String stackId = stackSummary.getId();
                String applicationName = getAppName(OracleCloudAccount.getInstance().getResourceManagerClientProxy().getStackDetails(stackId)) ;
                appNames.add(applicationName);
            }
            Validator.appNames = appNames ;
        });



    }

    private String getAppName(Stack stackDetails) {
        return stackDetails.getVariables().get("application_name");
    }
}
