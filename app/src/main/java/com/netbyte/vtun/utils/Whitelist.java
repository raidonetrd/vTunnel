package com.netbyte.vtun.utils;

import com.netbyte.vtun.config.AppConst;

import java.util.ArrayList;
import java.util.List;

public class Whitelist {

    public static List<String> packageList = new ArrayList<>();

    static {
        packageList.add(AppConst.APP_PACKAGE_NAME);
        packageList.add("cn.xuexi.android");
        packageList.add("com.taobao.taobao");
        packageList.add("com.eg.android.AlipayGphone");
        packageList.add("com.cainiao.wireless");
        packageList.add("com.alibaba.android.rimet");
        packageList.add("com.tencent.mobileqq");
        packageList.add("com.tencent.mm");
        packageList.add("com.tencent.tmgp.sgame");
        packageList.add("com.tencent.karaoke");
        packageList.add("com.hpbr.bosszhipin");
        packageList.add("com.qiyi.video");
        packageList.add("com.sankuai.meituan");
        packageList.add("com.sankuai.meituan.takeoutnew");
        packageList.add("com.MobileTicket");
        packageList.add("com.anjuke.android.app");
        packageList.add("com.tencent.edu");
        packageList.add("com.xunmeng.pinduoduo");
        packageList.add("com.suning.mobile.ebuy");
        packageList.add("com.jingdong.app.mall");
        packageList.add("ctrip.android.view");
        packageList.add("com.achievo.vipshop");
        packageList.add("com.netease.newsreader.activity");
        packageList.add("com.ss.android.article.lite");
        packageList.add("com.ss.android.article.news");
        packageList.add("com.UCMobile");
        packageList.add("com.ss.android.ugc.aweme");
        packageList.add("com.sina.weibo");
        packageList.add("com.greenpoint.android.mc10086.activity");
        packageList.add("com.sinovatech.unicom.ui");
        packageList.add("com.baidu.tieba");
        packageList.add("com.baidu.input_huawei");
        packageList.add("com.qihoo.appstore");
    }
}
