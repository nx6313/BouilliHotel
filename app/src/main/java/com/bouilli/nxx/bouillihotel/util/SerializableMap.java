package com.bouilli.nxx.bouillihotel.util;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableMap implements Serializable {
    private Map<String, Object[]> map;

    public Map<String, Object[]> getMap(){
        return map;
    }

    public void setMap(Map<String, Object[]> map){
        this.map = map;
    }
}
