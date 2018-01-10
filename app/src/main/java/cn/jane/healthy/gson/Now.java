package cn.jane.healthy.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jane on 2018/1/10.
 */

public class Now {
    @SerializedName("tmp")
    public String tmperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
