package com.boxxit.boxxit.library.parse.models.facebook;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Profile {

    public String id;
    public String name;
    public String birthday;
    public String email;
    public String first_name;
    public String gender;
    public Picture picture;
    public FacebookData friends = new FacebookData();

    public Date getDateFromFbDate () {

        if (birthday == null || birthday.equals("")) {
            return null;
        }

        String[] components = birthday.split("/");
        String inputFormat = "";
        if (components.length == 1) {
            inputFormat = "y";
        } else if (components.length == 2) {
            inputFormat = "MM/dd";
        } else {
            inputFormat = "MM/dd/yyyy";
        }

        SimpleDateFormat format = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());

        try {
            return format.parse(birthday);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getNextBirthday () {

        Date birthd = getDateFromFbDate();
        Date now = new Date();

        if (birthd != null) {

            Calendar bdayCal = Calendar.getInstance();
            bdayCal.setTime(birthd);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(now);

            int bday = bdayCal.get(Calendar.DAY_OF_MONTH);
            int bmonth = bdayCal.get(Calendar.MONTH) + 1;
            int bdayOfyear = bdayCal.get(Calendar.DAY_OF_YEAR);
            int cdayOfYear = nowCal.get(Calendar.DAY_OF_YEAR);
            int cyear = nowCal.get(Calendar.YEAR);
            int diff = bdayOfyear - cdayOfYear;
            int ryear = diff < 0 ? cyear + 1 : cyear;
            String inputFormat1 = "MM/dd/yyyy";
            SimpleDateFormat format1 = new SimpleDateFormat(inputFormat1, java.util.Locale.getDefault());

            try {
                String dt = (bmonth < 10 ? "0" + bmonth : bmonth) + "/" + (bday < 10 ? "0" + bday : bday) + "/" + ryear;
                Date date = format1.parse(dt);

                String inputFormat2 = "EEE, MMM d, yyyy";
                SimpleDateFormat format2 = new SimpleDateFormat(inputFormat2, java.util.Locale.getDefault());
                return format2.format(date);

            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isBirthdayToday () {
        String bday = getNextBirthday();

        if (bday != null) {
            String inputFormat = "EEE, MMM d, yyyy";
            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
            String nday = format.format(now);
            return bday.equals(nday);
        } else {
            return false;
        }
    }
}
