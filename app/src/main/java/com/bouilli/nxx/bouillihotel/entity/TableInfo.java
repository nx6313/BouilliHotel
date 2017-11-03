package com.bouilli.nxx.bouillihotel.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TableInfo {
    @Id(autoincrement = true)
    private Long id;
    private String groupCode;
    private String tableNo;
    private String tableName;
    private Integer tableStatus;
    @Generated(hash = 30396021)
    public TableInfo(Long id, String groupCode, String tableNo, String tableName,
            Integer tableStatus) {
        this.id = id;
        this.groupCode = groupCode;
        this.tableNo = tableNo;
        this.tableName = tableName;
        this.tableStatus = tableStatus;
    }
    @Generated(hash = 1659288981)
    public TableInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGroupCode() {
        return this.groupCode;
    }
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
    public String getTableNo() {
        return this.tableNo;
    }
    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }
    public String getTableName() {
        return this.tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public Integer getTableStatus() {
        return this.tableStatus;
    }
    public void setTableStatus(Integer tableStatus) {
        this.tableStatus = tableStatus;
    }
}
