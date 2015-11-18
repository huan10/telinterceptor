package com.seal.telinterceptor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by huan on 2015/11/12.
 */
public class PhoneChecker {
    private static final String URL = "http://apis.baidu.com/baidu_mobile_security/phone_number_service/phone_information_query";

    private ArrayList<String> mContactList;
    private static PhoneChecker sPhoneCheckerInstance;

    public interface JunkCheckerListener {
        void onJunk(PhoneInfo phoneInfo);
    }

    public static PhoneChecker getInstance() {
        if(sPhoneCheckerInstance == null) {
            sPhoneCheckerInstance = new PhoneChecker();
        }

        return sPhoneCheckerInstance;
    }

    public void init(Context context) {
        mContactList = getPhoneNum(context);
    }

    public void isJunk(Context context, final String phone, final JunkCheckerListener listener) {
        if(mContactList == null || mContactList.size() == 0) {
            init(context);
        }

        if(TextUtils.isEmpty(phone) || mContactList == null) {
            return;
        }

        if(mContactList.contains(phone)) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                String jsonResult = PhoneChecker.getInstance().request(phone);
                if (jsonResult != null) {
                    try {
                        PhoneInfo phoneInfo = new PhoneInfo();

                        JSONObject jsonObject = new JSONObject(jsonResult);
                        JSONObject jsonResp = jsonObject.getJSONObject("response");
                        JSONObject jsonData = jsonResp.getJSONObject(phone);
                        if (jsonData != null) {
                            String type = getJsonValue(jsonData, "type");
                            if ("report".equals(type)) {
                                int count = jsonData.getInt("count");
                                phoneInfo.count = count;
                            }

                            phoneInfo.name = getJsonValue(jsonData, "name");

                            JSONObject jsonLocation = jsonData.getJSONObject("location");
                            if (jsonLocation != null) {
                                String province = jsonLocation.getString("province");
                                String operators = jsonLocation.getString("operators");
                                String city = jsonLocation.getString("city");

                                phoneInfo.operator = operators;
                                phoneInfo.location = province + " " + city;
                            }
                        }

                        if (listener != null) {
                            listener.onJunk(phoneInfo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private String getJsonValue(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {

        }

        return "";
    }

    public String request(String mobile) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();

        String httpArg = "tel=" + mobile + "&location=true";
        String httpUrl = URL + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "edf121a8d6bc90badd151aa164d36ca9");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<String> getPhoneNum(Context context) {
        ArrayList<String> numList = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phone.moveToNext()) {
                String strPhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if(!TextUtils.isEmpty(strPhoneNumber)) {
                    numList.add(strPhoneNumber.replaceAll("[ -]", ""));
                }
            }

            phone.close();
        }
        cursor.close();
        return numList;
    }
}
