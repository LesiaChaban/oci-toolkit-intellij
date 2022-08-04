package com.oracle.oci.intellij.ui.database.model;

public abstract class VCNBasedAccessControlType extends AccessControlType {

    public VCNBasedAccessControlType(AccessControlType.Types type) {
        super(Category.VCN_BASED, type);
    }

    public AccessControlType.Types getType() {
        return super.getType();
    }

    public String getTypeLabel() {
        return this.getType().getLabel();
    }

}
