package in.anandroid.walletapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private Activity act = MainActivity.this;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        extractMerchantNameFromSmsMod();

        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getAllSms("inbox"); // Get all sms from inbox
            }
        }, 2000);

    }

    private void extractMerchantNameFromSmsMod() {
        try {

            String mMessage = "Thank you for using your SBI Debit Card 622XX3950 for a purchase worth Rs1745.34 on POS 02PL00000025876 in M/S MAX HYPER MARKET P txn 000475971033.";
            Pattern regEx = Pattern.compile("(?i)(?:\\sat|\\sin|\\sInfo.\\s*)([A-Za-z0-9*/. ]*\\s?-?\\s)");
            // Pattern regEx = Pattern.compile("(?i)(?:RS|INR|MRP)([0-9*/.]*\\d?-?\\d)");
            // Find instance of pattern matches
            Matcher m = regEx.matcher(mMessage);

            if (m.find()) {
                String mMerchantName = m.group();
                int length = m.groupCount();
                Log.e(TAG, "MERCHANT:" + mMerchantName + "-- " + mMerchantName.length());
                mMerchantName = mMerchantName.replaceAll("^\\s+|\\s+$", "");//trim from start and end
                mMerchantName = mMerchantName.replace("at", "");//replace at from Merchant
                mMerchantName = mMerchantName.replaceAll("in", "");//replace an from Merchant
                mMerchantName = mMerchantName.replaceAll("Info.", "");//replace Info. from Merchant
                mMerchantName = mMerchantName.replaceAll("\\D+", "");
                Log.e(TAG, "MERCHANT NAME:" + mMerchantName + "-- " + mMerchantName.length());

                NumberFormat format = NumberFormat.getInstance();
                format.setMaximumFractionDigits(2);
                Currency currency = Currency.getInstance("INR");
                format.setCurrency(currency);

                Log.e(TAG, "USD Amount: " + format.format(mMerchantName));

            } else {
                Log.e(TAG, "MATCH NOTFOUND");
            }

            extractDebitedAmountFromSmsMod(mMessage);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void extractDebitedAmountFromSmsMod(String message) {
        try {
            // Pattern regEx = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)");
            Pattern regEx = Pattern.compile("(?i)(?:RS|RS |INR |MRP )([0-9*/.]*\\d?-?\\d)");

            // Find instance of pattern matches
            Matcher m = regEx.matcher(message);
            if (m.find()) {
                String mDebitedAmount = m.group();
                Log.i("Before AMOUNT: ", mDebitedAmount);
                mDebitedAmount = mDebitedAmount.replaceAll("^\\s+|\\s+$", "");//trim from start and end
                Log.i("After AMOUNT: ", mDebitedAmount);
            } else {
                Log.e("SmsModReceiver", "MATCH NOTFOUND");

            }
        } catch (Exception e) {
            Log.i("Exception: ", e.toString());
        }
    }


    public List<SmsMod> getAllSms(String folderName) {
        List<SmsMod> lstSmsMod = new ArrayList<SmsMod>();
        SmsMod objSmsMod;
        Uri message = Uri.parse("content://sms/" + folderName);
        ContentResolver cr = act.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        //  act.startManagingCursor(c);
        int totalSmsMod = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSmsMod; i++) {

                objSmsMod = new SmsMod();
                objSmsMod.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSmsMod.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                objSmsMod.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSmsMod.setReadState(c.getString(c.getColumnIndex("read")));
                objSmsMod.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                String addressSms = c.getString(c.getColumnIndexOrThrow("address"));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSmsMod.setFolderName("inbox");
                } else {
                    objSmsMod.setFolderName("sent");
                }
                if (addressSms.contains("HDFC")) {
                    count++;
                    Log.e(TAG, "SMS Total Count : " + count);
                    Log.e(TAG, "SMS ADDRESS: " + addressSms);
                    // Log.e(TAG, "SMS ID: " + c.getString(c.getColumnIndex OrThrow("_id")));
                    Log.e(TAG, "SMS BODY: " + c.getString(c.getColumnIndexOrThrow("body")));
                    Log.e(TAG, "SMS DATE: " + toDateConvert(c.getString(c.getColumnIndexOrThrow("date"))));

                    extractMerchatName(c.getString(c.getColumnIndexOrThrow("body")));

                } else {

                }
                lstSmsMod.add(objSmsMod);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SmsMod in " + folderName);
        // }
        c.close();

        return lstSmsMod;
    }


    private String toDateConvert(String date) {
        Long timestamp = Long.parseLong(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        Date finaldate = calendar.getTime();
        return finaldate.toString();
    }

    private void extractMerchatName(String mMessage) {
        try {

            //String mMessage = "Thank you for using your SBI Debit Card 622XX3950 for a purchase worth Rs1745.34 on POS 02PL00000025876 in M/S MAX HYPER MARKET P txn 000475971033.";
            //Pattern regEx = Pattern.compile("(?i)(?:\\sat|\\sin |\\sInfo.\\s* )([A-Z0-9*/. ]*\\s?-?\\s)");  // Original
            Pattern regEx = Pattern.compile("(?i)(?:\\sat|\\sInfo.\\s* )([A-Z0-9*/. ]*\\s?-?\\s)");
            //  Pattern regEx = Pattern.compile("(?i)(?:\\sat |\\sin |\\sInfo.\\s* )([a-zA-Z0-9*/. ] *\\s?-?\\s)");
            // Pattern regEx = Pattern.compile("(?i)(?:RS|INR|MRP)([0-9*/.]*\\d?-?\\d)");
            // Find instance of pattern matches
            Matcher m = regEx.matcher(mMessage);

            if (m.find()) {
                String mMerchantName = m.group();
                int length = m.groupCount();
                Log.e(TAG, "MERCHANT Before:" + mMerchantName + "-- " + mMerchantName.length());
                mMerchantName = mMerchantName.replace("at", "");//replace at from Merchant
                mMerchantName = mMerchantName.replaceAll("in", "");//replace an from Merchant
                mMerchantName = mMerchantName.replaceAll("Info.", "");//replace Info. from Merchant
                mMerchantName = mMerchantName.replaceAll("^\\s+|\\s+$", "");//trim from start and end
                mMerchantName = findLowerCase(mMerchantName);

                // mMerchantName = mMerchantName.replaceAll("\\D+", "");
                Log.e(TAG, "MERCHANT NAME:" + mMerchantName + "-- " + mMerchantName.length());
                Log.e(TAG, "MERCHANT Breaked:" + mMerchantName);

                /*NumberFormat format = NumberFormat.getInstance();
                format.setMaximumFractionDigits(2);
                Currency currency = Currency.getInstance("INR");
                format.setCurrency(currency);
                Log.e(TAG, "USD Amount: " + format.format(mMerchantName));*/

            } else {
                Log.e(TAG, "MATCH NOTFOUND");
            }

            extractDebitedAmountFromSmsMod(mMessage);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    // For Find out After the
    private String findLowerCase(String txt) {

        for (int i = 0; i < txt.length(); i++) {

            // if small letters came it will break and collect 0 to that (i) position
            if (isLowerCase(txt.charAt(i))) {
                txt = txt.substring(0, i);
                break;
            }
        }
        return txt;
    }

    static boolean isLowerCase(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

}
