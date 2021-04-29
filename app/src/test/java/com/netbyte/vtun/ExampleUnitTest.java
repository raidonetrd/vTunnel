package com.netbyte.vtun;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;

public class ExampleUnitTest {
    @Test
    public void test() throws IOException {
        String host = "https://1.1.1.1/dns-query";
        String query = "google.com.";
        DohResolver dohResolver = new DohResolver(host);
        System.out.println(dohResolver.queryIP(query));
    }

}