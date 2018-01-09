package cn.jane.healthy.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Jane on 2018/1/9.
 */

public class Provice extends DataSupport {
    private int id;
    private String proviceName;
    private int proviceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProviceName() {
        return proviceName;
    }

    public void setProviceName(String proviceName) {
        this.proviceName = proviceName;
    }

    public int getProviceCode() {
        return proviceCode;
    }

    public void setProviceCode(int proviceCode) {
        this.proviceCode = proviceCode;
    }
}
