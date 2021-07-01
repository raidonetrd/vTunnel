package com.netbyte.vtunnel.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PackageUtil {
    private static final String base64AppList = "Y24uCmNvbS50ZW5jZW50Lgpjb20uc21pbGUuCmNvbS5zcy4KY29tLmR1b3dhbi4KY29tLlVDTW9iaWxlLgpjb20ueHVubWVuZy4KdHYuZGFubWFrdS4KY29tLm1ldGEuCmNvbS5xdWFyay4KY29tLnNuZGEuCmNvbS5odW5hbnR2Lgpjb20uc2Fua3VhaS4KY29tLm5ldGVhc2UuCmNvbS5rdWdvdS4KY29tLmJhaWR1Lgpjb20udGFvYmFvLgpjb20ueHVubGVpLgpjb20ucWl5aS4KY29tLmxlMTIzLgpjb20uc2luYS4KY29tLmppbmdkb25nLgpjb20uc2hpemh1YW5nLgpjb20uZWcuCmNvbS55b3VrdS4KY29tLnBlb3BsZXRlY2guCmNvbS5pY2JjLgpjb20udW5pb25wYXkuCmNvbS5hbmRyb2lkLmJhbmthYmMuCmNvbS5jaGluYW13b3JsZC4KY29tLnlpdG9uZy4KY29tLmpkLgpjb20uc2lub3ZhdGVjaC4KY29tLmdyZWVucG9pbnQuCmNvbS5jdC4KY29tLmNhaW5pYW8uCmNvbS5hbGliYWJhLgpjb20udG1yaS4KY29tLnNkdS4KY29tLm1iLgpjb20uem1zLgptZS5lbGUKY21iLnBiCmNvbS5ocGJyLgpjb20uYWxwaGEu";
    public static List<String> bypassPackageList = new ArrayList<>();

    static {
        String decodeAppList = new String(Base64.getDecoder().decode(base64AppList.getBytes(StandardCharsets.UTF_8)));
        String[] appList = decodeAppList.split("\n");
        if (appList.length > 0) {
            bypassPackageList.addAll(Arrays.asList(appList));
        }
    }
}
