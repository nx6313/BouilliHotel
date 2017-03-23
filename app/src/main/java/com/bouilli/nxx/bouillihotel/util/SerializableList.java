package com.bouilli.nxx.bouillihotel.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableList implements Serializable {
    private List<Map<String, Object[]>> list = null;

    public List<Map<String, Object[]>> getList(){
        return list;
    }

    public void setList(List<Map<String, Object[]>> list){
        this.list = list;
    }
}
