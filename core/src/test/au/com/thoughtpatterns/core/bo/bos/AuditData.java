package au.com.thoughtpatterns.core.bo.bos;

import java.util.Date;

import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Util;


public class AuditData extends PersistentObject {

    private static final long serialVersionUID = 1L;

    @PersistentField
    private String txnUser;
    
    @PersistentField 
    private String txnName;
    
    @PersistentField
    private Date txnTimestamp;
    
    
    @Generated
    public String getTxnUser() {
        return txnUser;
    }

    @Generated
    public void setTxnUser(String txnUser) {
        if (Util.equals(this.txnUser, txnUser)) {
            return;
        }
        this.txnUser = txnUser;
        fireChanged();
    }

    @Generated
    public String getTxnName() {
        return txnName;
    }

    @Generated
    public void setTxnName(String txnName) {
        if (Util.equals(this.txnName, txnName)) {
            return;
        }
        this.txnName = txnName;
        fireChanged();
    }

    @Generated
    public Date getTxnTimestamp() {
        return txnTimestamp;
    }

    @Generated
    public void setTxnTimestamp(Date txnTimestamp) {
        if (Util.equals(this.txnTimestamp, txnTimestamp)) {
            return;
        }
        this.txnTimestamp = txnTimestamp;
        fireChanged();
    }

}
