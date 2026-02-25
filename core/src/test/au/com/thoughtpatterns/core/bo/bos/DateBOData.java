package au.com.thoughtpatterns.core.bo.bos;

import au.com.thoughtpatterns.core.bo.PersistentClass;
import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.util.AusDollar;
import au.com.thoughtpatterns.core.util.BusinessDate;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Property;
import au.com.thoughtpatterns.core.util.Util;

@PersistentClass(table="test_date")
public class DateBOData extends PersistentObject {

    private static final long serialVersionUID = 1L;
    public static final @Generated @Property String P_finishDate = "finishDate";
    public static final @Generated @Property String P_startDate = "startDate";
    public static final @Generated @Property String P_amount = "amount";

    @PersistentField(column="start_date")
    private BusinessDate startDate;

    @PersistentField(column="end_date")
    private BusinessDate finishDate;
    
    @PersistentField(column="payment_cents")
    private AusDollar amount;
    
    @Generated @Property
    public BusinessDate getStartDate() {
        return startDate;
    }

    @Generated
    public void setStartDate(BusinessDate startDate) {
        if (Util.equals(this.startDate, startDate)) {
            return;
        }
        this.startDate = startDate;
        fireChanged();
    }

    @Generated @Property
    public BusinessDate getFinishDate() {
        return finishDate;
    }

    @Generated
    public void setFinishDate(BusinessDate finishDate) {
        if (Util.equals(this.finishDate, finishDate)) {
            return;
        }
        this.finishDate = finishDate;
        fireChanged();
    }

    @Generated @Property
    public AusDollar getAmount() {
        return amount;
    }

    @Generated
    public void setAmount(AusDollar amount) {
        if (Util.equals(this.amount, amount)) {
            return;
        }
        this.amount = amount;
        fireChanged();
    }

}
