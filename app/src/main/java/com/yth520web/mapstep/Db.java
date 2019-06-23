package com.yth520web.mapstep;

import org.litepal.crud.DataSupport;

public class Db extends DataSupport {
    /**
     * 保存用户的身高体重，用于计算卡路里
     */
    float userHeight;
    float userWeight;
    public float getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(float userHeight) {
        this.userHeight = userHeight;
    }

    public float getUserWeight() {
        return userWeight;
    }

    public void setUserWeight(float userWeight) {
        this.userWeight = userWeight;
    }


}
