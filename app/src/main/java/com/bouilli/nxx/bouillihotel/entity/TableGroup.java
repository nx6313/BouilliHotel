package com.bouilli.nxx.bouillihotel.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class TableGroup {
    @Id(autoincrement = true)
    private Long id;
    private String tableGroupName;
    private String tableGroupCode;
    private String tableGroupNo;
    @Generated(hash = 1758863022)
    public TableGroup(Long id, String tableGroupName, String tableGroupCode,
            String tableGroupNo) {
        this.id = id;
        this.tableGroupName = tableGroupName;
        this.tableGroupCode = tableGroupCode;
        this.tableGroupNo = tableGroupNo;
    }
    @Generated(hash = 1895168193)
    public TableGroup() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTableGroupName() {
        return this.tableGroupName;
    }
    public void setTableGroupName(String tableGroupName) {
        this.tableGroupName = tableGroupName;
    }
    public String getTableGroupCode() {
        return this.tableGroupCode;
    }
    public void setTableGroupCode(String tableGroupCode) {
        this.tableGroupCode = tableGroupCode;
    }
    public String getTableGroupNo() {
        return this.tableGroupNo;
    }
    public void setTableGroupNo(String tableGroupNo) {
        this.tableGroupNo = tableGroupNo;
    }
}
