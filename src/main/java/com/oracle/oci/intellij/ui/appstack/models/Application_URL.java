package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;

public class Application_URL extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean create_fqdn;

    private java.lang.Object dns_compartment;

    private java.lang.Object zone;

    private java.lang.String subdomain;

    private java.lang.Object certificate_ocid;

    @PropertyOrder(1)
    @VariableMetaData(title="Create DNS record",description="If you check this checkbox the stack will create a DNS record that will resolve to the load balancer's IP address.",defaultVal="true",type="boolean",required=true)

    public boolean isCreate_fqdn() {
        return create_fqdn;
    }

    public void setCreate_fqdn(boolean newValue) throws PropertyVetoException {
        Object oldValue = this.create_fqdn;
        this.create_fqdn = newValue;
        pcs.firePropertyChange("create_fqdn", oldValue, newValue);
        vcp.fireVetoableChange("create_fqdn", oldValue, newValue);
    }
    @PropertyOrder(2)
    @VariableMetaData(title="DNS and Certificate compartement",description="Compartment containing the DNS Zone and the Certificate",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="create_fqdn")

    public Object getDns_compartment() {
        return dns_compartment;
    }

    public void setDns_compartment(Object newValue) throws PropertyVetoException {
        Object oldValue = this.dns_compartment;
        this.dns_compartment = newValue;
        pcs.firePropertyChange("dns_compartment", oldValue, newValue);
        vcp.fireVetoableChange("dns_compartment", oldValue, newValue);
    }
    @PropertyOrder(3)
    @VariableMetaData(title="DNS Zone",description="Domain name in which the host name will be created.",type="oci:dns:zone:id",required=true,visible="create_fqdn")
    public Object getZone() {
        return zone;
    }

    public void setZone(Object newValue) throws PropertyVetoException {
        Object oldValue = this.zone;
        this.zone = newValue;
        pcs.firePropertyChange("zone", oldValue, newValue);
        vcp.fireVetoableChange("zone", oldValue, newValue);

    }
    @PropertyOrder(4)
    @VariableMetaData(title="Host name",description="The host name will be created on the selected Zone and will resolve to the the load balancer's IP address.",type="string",required=true,visible="create_fqdn")
    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String newValue) throws PropertyVetoException {
        Object oldValue = this.subdomain;
        this.subdomain = newValue;
        pcs.firePropertyChange("subdomain", oldValue, newValue);
        vcp.fireVetoableChange("subdomain", oldValue, newValue);
    }
    @PropertyOrder(5)
    @VariableMetaData(title="Certificate OCID",description="You must have a SSL certificate available in OCI Certificates service. Provide the certificate OCID for the host name,that will be used to configure the load balancer.",type="oci:certificatesmanagement:certificate:id",required=true,visible="create_fqdn")
    public Object getCertificate_ocid() {
        return certificate_ocid;
    }

    public void setCertificate_ocid(Object newValue) throws PropertyVetoException {
        Object oldValue = this.certificate_ocid;
        this.certificate_ocid = newValue;
        pcs.firePropertyChange("certificate_ocid", oldValue, newValue);
        vcp.fireVetoableChange("certificate_ocid", oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}