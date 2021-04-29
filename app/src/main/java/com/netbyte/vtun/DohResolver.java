package com.netbyte.vtun;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class DohResolver {

    private String host;

    public DohResolver(String host) {
        this.host = host;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Message query(Message query, Integer timeout) throws IOException {
        String encodedQuery = Base64.getUrlEncoder().withoutPadding().encodeToString(query.toWire());
        Message response = makeGetRequest(encodedQuery, timeout);
        return response;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String queryIP(String domain) throws IOException {
        Record queryTxtRecord = Record.newRecord(new Name(domain), Type.A, DClass.IN);
        Message queryMessage = Message.newQuery(queryTxtRecord);
        String encodedQuery = Base64.getUrlEncoder().withoutPadding().encodeToString(queryMessage.toWire());
        Message response = makeGetRequest(encodedQuery, 5000);
        final Record[] answers = response.getSectionArray(Section.ANSWER);
        if (answers == null || answers.length <= 0) {
            return "";
        }
        String ip = answers[0].rdataToString();
        if (ip.endsWith(".")) {
            ip = ip.substring(0, ip.length() - 1);
        }
        return ip;
    }

    private Message makeGetRequest(String encodedQuery, Integer timeout) throws IOException {
        HttpsURLConnection con = null;
        try {
            URL myUrl = new URL(host + "?dns=" + encodedQuery);
            con = (HttpsURLConnection) myUrl.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout);
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("content-type", "application/dns-message");

            byte[] readBytes = new byte[65535];
            con.getInputStream().read(readBytes);
            return new Message(readBytes);
        } finally {
            con.disconnect();
        }
    }
}