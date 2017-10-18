package com.bouilli.nxx.bouillihotel.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by nx6313 on 2017/10/18.
 */
@Entity
public class TableGroup {
    @Id(autoincrement = true)
    private long id;
    private String tableGroupName;
    private String tableGroupNo;
    @Generated(hash = 892577129)
    public TableGroup(long id, String tableGroupName, String tableGroupNo) {
        this.id = id;
        this.tableGroupName = tableGroupName;
        this.tableGroupNo = tableGroupNo;
    }
    @Generated(hash = 1895168193)
    public TableGroup() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTableGroupName() {
        return this.tableGroupName;
    }
    public void setTableGroupName(String tableGroupName) {
        this.tableGroupName = tableGroupName;
    }
    public String getTableGroupNo() {
        return this.tableGroupNo;
    }
    public void setTableGroupNo(String tableGroupNo) {
        this.tableGroupNo = tableGroupNo;
    }
}
