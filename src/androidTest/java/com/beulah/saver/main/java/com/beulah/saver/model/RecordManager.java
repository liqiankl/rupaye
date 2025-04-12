package com.beulah.saver.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperToast;
import com.beulah.saver.BuildConfig;
import com.beulah.saver.R;
import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.db.DB;
import com.beulah.saver.util.RupayeToast;
import com.beulah.saver.util.RupayeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 伟平 on 2015/10/20.
 */

public class RecordManager {

    private static RecordManager recordManager = null;

    private static DB db;

    // the selected values in list activity
    public static Double SELECTED_SUM;
    public static List<RupayeRecord> SELECTED_RECORDS;

    public static Integer SUM;
    public static List<RupayeRecord> RECORDS;
    public static List<Tag> TAGS;
    public static Map<Integer, String> TAG_NAMES;

    public static boolean RANDOM_DATA = false;
    private final int RANDOM_DATA_NUMBER_ON_EACH_DAY = 3;
    private final int RANDOM_DATA_EXPENSE_ON_EACH_DAY = 30;

    private static boolean FIRST_TIME = true;

    public static int SAVE_TAG_ERROR_DATABASE_ERROR = -1;
    public static int SAVE_TAG_ERROR_DUPLICATED_NAME = -2;

    public static int DELETE_TAG_ERROR_DATABASE_ERROR = -1;
    public static int DELETE_TAG_ERROR_TAG_REFERENCE = -2;

// constructor//////////////////////////////////////////////////////////////////////////////////////
    private RecordManager(Context context) {
        try {
            db = db.getInstance(context);
            if (BuildConfig.DEBUG) if (BuildConfig.DEBUG) Log.d("Rupaye", "db.getInstance(context) S");
        } catch(IOException e) {
            e.printStackTrace();
        }
        if (FIRST_TIME) {
// if the app starts firstly, create tags///////////////////////////////////////////////////////////
            SharedPreferences preferences =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE);
            if (preferences.getBoolean("FIRST_TIME", true)) {
                createTags();
                SharedPreferences.Editor editor =
                        context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit();
                editor.putBoolean("FIRST_TIME", false);
                editor.commit();
            }
        }
        if (RANDOM_DATA) {

            SharedPreferences preferences =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE);
            if (preferences.getBoolean("RANDOM", false)) {
                return;
            }

            randomDataCreater();

            SharedPreferences.Editor editor =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit();
            editor.putBoolean("RANDOM", true);
            editor.commit();

        }
    }

// getInstance//////////////////////////////////////////////////////////////////////////////////////
    public synchronized static RecordManager getInstance(Context context) {
        if (RECORDS == null || TAGS == null || TAG_NAMES == null || SUM == null || recordManager == null) {
            SUM = 0;
            RECORDS = new LinkedList<>();
            TAGS = new LinkedList<>();
            TAG_NAMES = new HashMap<>();
            recordManager = new RecordManager(context);

            db.getData();

            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Load " + RECORDS.size() + " records S");
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Load " + TAGS.size() + " tags S");
            }

            TAGS.add(0, new Tag(-1, "Sum Histogram", -4));
            TAGS.add(0, new Tag(-2, "Sum Pie", -5));

            for (Tag tag : TAGS) TAG_NAMES.put(tag.getId(), tag.getName());

            sortTAGS();
        }
        return recordManager;
    }

// saveRecord///////////////////////////////////////////////////////////////////////////////////////
    public static long saveRecord(final RupayeRecord RupayeRecord) {
        long insertId = -1;
        // this is a new RupayeRecord, which is not uploaded
        RupayeRecord.setIsUploaded(false);
//        User user = BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class);
//        if (user != null) RupayeRecord.setUserId(user.getObjectId());
//        else RupayeRecord.setUserId(null);
        if (BuildConfig.DEBUG)
            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save " + RupayeRecord.toString() + " S");
        insertId = db.saveRecord(RupayeRecord);
        if (insertId == -1) {
            if (BuildConfig.DEBUG)
                if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save the above RupayeRecord FAIL!");
            RupayeToast.getInstance()
                    .showToast(R.string.save_failed_locale, SuperToast.Background.RED);
        } else {
            if (BuildConfig.DEBUG)
                if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save the above RupayeRecord SUCCESSFULLY!");
            RECORDS.add(RupayeRecord);
            SUM += (int) RupayeRecord.getMoney();
//            if (user != null) {
//                // already login
//                RupayeRecord.save(RupayeApplication.getAppContext(), new SaveListener() {
//                    @Override
//                    public void onSuccess() {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save online " + RupayeRecord.toString() + " S");
//                        RupayeRecord.setIsUploaded(true);
//                        RupayeRecord.setLocalObjectId(RupayeRecord.getObjectId());
//                        db.updateRecord(RupayeRecord);
//                        RupayeToast.getInstance()
//                                .showToast(R.string.save_successfully_online, SuperToast.Background.BLUE);
//                    }
//                    @Override
//                    public void onFailure(int code, String msg) {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save online " + RupayeRecord.toString() + " F");
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveRecord: Save online msg: " + msg + " code " + code);
//                        RupayeToast.getInstance()
//                                .showToast(R.string.save_failed_online, SuperToast.Background.RED);
//                    }
//                });
//            } else {
//                RupayeToast.getInstance()
//                        .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
//            }
            RupayeToast.getInstance()
                    .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
        }
        return insertId;
    }

// save tag/////////////////////////////////////////////////////////////////////////////////////////
    public static int saveTag(Tag tag) {
        int insertId = -1;
        if (BuildConfig.DEBUG) {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.saveTag: " + tag.toString());
        }
        boolean duplicatedName = false;
        for (Tag t : TAGS) {
            if (t.getName().equals(tag.getName())) {
                duplicatedName = true;
                break;
            }
        }
        if (duplicatedName) {
            return SAVE_TAG_ERROR_DUPLICATED_NAME;
        }
        insertId = db.saveTag(tag);
        if (insertId == -1) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Save the above tag FAIL!");
                return SAVE_TAG_ERROR_DATABASE_ERROR;
            }
        } else {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Save the above tag SUCCESSFULLY!");
            }
            TAGS.add(tag);
            TAG_NAMES.put(tag.getId(), tag.getName());
            sortTAGS();
        }
        return insertId;
    }

// delete a RupayeRecord//////////////////////////////////////////////////////////////////////////////////
    public static long deleteRecord(final RupayeRecord RupayeRecord, boolean deleteInList) {
        long deletedNumber = db.deleteRecord(RupayeRecord.getId());
        if (deletedNumber > 0) {
            if (BuildConfig.DEBUG) Log.d("Rupaye",
                    "recordManager.deleteRecord: Delete " + RupayeRecord.toString() + " S");
            User user = BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class);
            // if we can delete the RupayeRecord from server
//            if (user != null && RupayeRecord.getLocalObjectId() != null) {
//                RupayeRecord.delete(RupayeApplication.getAppContext(), new DeleteListener() {
//                    @Override
//                    public void onSuccess() {
//                        if (BuildConfig.DEBUG) {
//                            if (BuildConfig.DEBUG) Log.d("Rupaye",
//                                    "recordManager.deleteRecord: Delete online " + RupayeRecord.toString() + " S");
//                        }
//                        RupayeToast.getInstance()
//                                .showToast(R.string.delete_successfully_online, SuperToast.Background.BLUE);
//                    }
//                    @Override
//                    public void onFailure(int code, String msg) {
//                        if (BuildConfig.DEBUG) {
//                            if (BuildConfig.DEBUG) Log.d("Rupaye",
//                                    "recordManager.deleteRecord: Delete online " + RupayeRecord.toString() + " F");
//                        }
//                        RupayeToast.getInstance()
//                                .showToast(R.string.delete_failed_online, SuperToast.Background.RED);
//                    }
//                });
//            } else {
//                RupayeToast.getInstance()
//                        .showToast(R.string.delete_successfully_locale, SuperToast.Background.BLUE);
//            }
            RupayeToast.getInstance()
                    .showToast(R.string.delete_successfully_locale, SuperToast.Background.BLUE);
            // update RECORDS list and SUM
            SUM -= (int) RupayeRecord.getMoney();
            if (deleteInList) {
                int size = RECORDS.size();
                for (int i = 0; i < RECORDS.size(); i++) {
                    if (RECORDS.get(i).getId() == RupayeRecord.getId()) {
                        RECORDS.remove(i);
                        if (BuildConfig.DEBUG) Log.d("Rupaye",
                                "recordManager.deleteRecord: Delete in RECORD " + RupayeRecord.toString() + " S");
                        break;
                    }
                }
            }
        } else {
            if (BuildConfig.DEBUG) Log.d("Rupaye",
                    "recordManager.deleteRecord: Delete " + RupayeRecord.toString() + " F");
            RupayeToast.getInstance()
                    .showToast(R.string.delete_failed_locale, SuperToast.Background.RED);
        }


        return RupayeRecord.getId();
    }

    public static int deleteTag(int id) {
        int deletedId = -1;
        if (BuildConfig.DEBUG) Log.d("Rupaye",
                "Manager: Delete tag: " + "Tag(id = " + id + ", deletedId = " + deletedId + ")");
        boolean tagReference = false;
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeRecord.getTag() == id) {
                tagReference = true;
                break;
            }
        }
        if (tagReference) {
            return DELETE_TAG_ERROR_TAG_REFERENCE;
        }
        deletedId = db.deleteTag(id);
        if (deletedId == -1) {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "Delete the above tag FAIL!");
            return DELETE_TAG_ERROR_DATABASE_ERROR;
        } else {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "Delete the above tag SUCCESSFULLY!");
            for (Tag tag : TAGS) {
                if (tag.getId() == deletedId) {
                    TAGS.remove(tag);
                    break;
                }
            }
            TAG_NAMES.remove(id);
            sortTAGS();
        }
        return deletedId;
    }

    private static int p;
    public static long updateRecord(final RupayeRecord RupayeRecord) {
        long updateNumber = db.updateRecord(RupayeRecord);
        if (updateNumber <= 0) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord " + RupayeRecord.toString() + " F");
            }
            RupayeToast.getInstance().showToast(R.string.update_failed_locale, SuperToast.Background.RED);
        } else {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord " + RupayeRecord.toString() + " S");
            }
            p = RECORDS.size() - 1;
            for (; p >= 0; p--) {
                if (RECORDS.get(p).getId() == RupayeRecord.getId()) {
                    SUM -= (int)RECORDS.get(p).getMoney();
                    SUM += (int) RupayeRecord.getMoney();
                    RECORDS.get(p).set(RupayeRecord);
                    break;
                }
            }
            RupayeRecord.setIsUploaded(false);
//            User user = BmobUser
//                    .getCurrentUser(RupayeApplication.getAppContext(), User.class);
//            if (user != null) {
//                // already login
//                if (RupayeRecord.getLocalObjectId() != null) {
//                    // this RupayeRecord has been push to the server
//                    RupayeRecord.setUserId(user.getObjectId());
//                    RupayeRecord.update(RupayeApplication.getAppContext(),
//                            RupayeRecord.getLocalObjectId(), new UpdateListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord update online " + RupayeRecord.toString() + " S");
//                                    }
//                                    RupayeRecord.setIsUploaded(true);
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    db.updateRecord(RupayeRecord);
//                                    RupayeToast.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord update online " + RupayeRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord update online code" + code + " msg " + msg );
//                                    }
//                                    RupayeToast.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                } else {
//                    // this RupayeRecord has not been push to the server
//                    RupayeRecord.setUserId(user.getObjectId());
//                    RupayeRecord.save(RupayeApplication.getAppContext(), new SaveListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord save online " + RupayeRecord.toString() + " S");
//                                    }
//                                    RupayeRecord.setIsUploaded(true);
//                                    RupayeRecord.setLocalObjectId(RupayeRecord.getObjectId());
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    RECORDS.get(p).setLocalObjectId(RupayeRecord.getObjectId());
//                                    db.updateRecord(RupayeRecord);
//                                    RupayeToast.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord save online " + RupayeRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateRecord save online code" + code + " msg " + msg );
//                                    }
//                                    RupayeToast.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                }
//            } else {
//                // has not login
//                db.updateRecord(RupayeRecord);
//                RupayeToast.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
//            }
            db.updateRecord(RupayeRecord);
            RupayeToast.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
        }
        return updateNumber;
    }

// update the records changed to server/////////////////////////////////////////////////////////////
    private static boolean isLastOne = false;
    public static long updateOldRecordsToServer() {
        long counter = 0;
        User user = BmobUser
                .getCurrentUser(RupayeApplication.getAppContext(), User.class);
        if (user != null) {
// already login////////////////////////////////////////////////////////////////////////////////////
            isLastOne = false;
            for (int i = 0; i < RECORDS.size(); i++) {
                if (i == RECORDS.size() - 1) isLastOne = true;
                final RupayeRecord RupayeRecord = RECORDS.get(i);
                if (!RupayeRecord.getIsUploaded()) {
// has been changed/////////////////////////////////////////////////////////////////////////////////
                    if (RupayeRecord.getLocalObjectId() != null) {
// there is an old RupayeRecord in server, we should update this RupayeRecord///////////////////////////////////
                        RupayeRecord.setUserId(user.getObjectId());
                        RupayeRecord.update(RupayeApplication.getAppContext(),
                                RupayeRecord.getLocalObjectId(), new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer update online " + RupayeRecord.toString() + " S");
                                        }
                                        RupayeRecord.setIsUploaded(true);
                                        RupayeRecord.setLocalObjectId(RupayeRecord.getObjectId());
                                        db.updateRecord(RupayeRecord);
// after updating, get the old records from server//////////////////////////////////////////////////
                                        if (isLastOne) getRecordsFromServer();
                                    }

                                    @Override
                                    public void onFailure(int code, String msg) {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer update online " + RupayeRecord.toString() + " F");
                                        }
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer update online code" + code + " msg " + msg );
                                        }
                                    }
                                });
                    } else {
                        counter++;
                        RupayeRecord.setUserId(user.getObjectId());
                        RupayeRecord.save(RupayeApplication.getAppContext(), new SaveListener() {
                            @Override
                            public void onSuccess() {
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer save online " + RupayeRecord.toString() + " S");
                                }
                                RupayeRecord.setIsUploaded(true);
                                RupayeRecord.setLocalObjectId(RupayeRecord.getObjectId());
                                db.updateRecord(RupayeRecord);
// after updating, get the old records from server//////////////////////////////////////////////////
                                if (isLastOne) getRecordsFromServer();
                            }

                            @Override
                            public void onFailure(int code, String msg) {
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer save online " + RupayeRecord.toString() + " F");
                                }
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer save online code" + code + " msg " + msg );
                                }
                            }
                        });
                    }
                }
            }
        } else {

        }

        if (BuildConfig.DEBUG) {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.updateOldRecordsToServer update " + counter + " records to server.");
        }

        if (RECORDS.size() == 0) getRecordsFromServer();

        return counter;
    }

    public static long updateTag(Tag tag) {
        int updateId = -1;
        if (BuildConfig.DEBUG) Log.d("Rupaye",
                "Manager: Update tag: " + tag.toString());
        updateId = db.updateTag(tag);
        if (updateId == -1) {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "Update the above tag FAIL!");
        } else {
            if (BuildConfig.DEBUG) Log.d("Rupaye", "Update the above tag SUCCESSFULLY!" + " - " + updateId);
            for (Tag t : TAGS) {
                if (t.getId() == tag.getId()) {
                    t.set(tag);
                    break;
                }
            }
            sortTAGS();
        }
        return updateId;
    }

//get records from server to local//////////////////////////////////////////////////////////////////
    private static long updateNum;
    public static long getRecordsFromServer() {
        updateNum = 0;
        BmobQuery<RupayeRecord> query = new BmobQuery<RupayeRecord>();
        query.addWhereEqualTo("userId",
                BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class).getObjectId());
        query.setLimit(Integer.MAX_VALUE);
        query.findObjects(RupayeApplication.getAppContext(), new FindListener<RupayeRecord>() {
            @Override
            public void onSuccess(List<RupayeRecord> object) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.getRecordsFromServer get " + object.size() + " records from server");
                }
                updateNum = object.size();
                for (RupayeRecord RupayeRecord : object) {
                    boolean exist = false;
                    for (int i = RECORDS.size() - 1; i >= 0; i--) {
                        if (RupayeRecord.getObjectId().equals(RECORDS.get(i).getLocalObjectId())) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        RupayeRecord newRupayeRecord = new RupayeRecord();
                        newRupayeRecord.set(RupayeRecord);
                        newRupayeRecord.setId(-1);
                        RECORDS.add(newRupayeRecord);
                    }
                }

                Collections.sort(RECORDS, new Comparator<RupayeRecord>() {
                    @Override
                    public int compare(RupayeRecord lhs, RupayeRecord rhs) {
                        if (lhs.getCalendar().before(rhs.getCalendar())) {
                            return -1;
                        } else if (lhs.getCalendar().after(rhs.getCalendar())) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });

                db.deleteAllRecords();

                SUM = 0;
                for (int i = 0; i < RECORDS.size(); i++) {
                    RECORDS.get(i).setLocalObjectId(RECORDS.get(i).getObjectId());
                    RECORDS.get(i).setIsUploaded(true);
                    db.saveRecord(RECORDS.get(i));
                    SUM += (int)RECORDS.get(i).getMoney();
                }

                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.getRecordsFromServer save " + RECORDS.size() + " records");
                }
            }
            @Override
            public void onError(int code, String msg) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("Rupaye", "recordManager.getRecordsFromServer error " + msg);
                }
            }
        });

        return updateNum;
    }

    public static int getCurrentMonthExpense() {
        Calendar calendar = Calendar.getInstance();
        Calendar left = RupayeUtil.GetThisMonthLeftRange(calendar);
        int monthSum = 0;
        for (int i = RECORDS.size() - 1; i >= 0; i--) {
            if (RECORDS.get(i).getCalendar().before(left)) break;
            monthSum += RECORDS.get(i).getMoney();
        }
        return monthSum;
    }

    public static List<RupayeRecord> queryRecordByTime(Calendar c1, Calendar c2) {
        List<RupayeRecord> list = new LinkedList<>();
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeRecord.isInTime(c1, c2)) {
                list.add(RupayeRecord);
            }
        }
        return list;
    }

    public static List<RupayeRecord> queryRecordByCurrency(String currency) {
        List<RupayeRecord> list = new LinkedList<>();
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeRecord.getCurrency().equals(currency)) {
                list.add(RupayeRecord);
            }
        }
        return list;
    }

    public static List<RupayeRecord> queryRecordByTag(int tag) {
        List<RupayeRecord> list = new LinkedList<>();
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeRecord.getTag() == tag) {
                list.add(RupayeRecord);
            }
        }
        return list;
    }

    public static List<RupayeRecord> queryRecordByMoney(double money1, double money2, String currency) {
        List<RupayeRecord> list = new LinkedList<>();
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeRecord.isInMoney(money1, money2, currency)) {
                list.add(RupayeRecord);
            }
        }
        return list;
    }

    public static List<RupayeRecord> queryRecordByRemark(String remark) {
        List<RupayeRecord> list = new LinkedList<>();
        for (RupayeRecord RupayeRecord : RECORDS) {
            if (RupayeUtil.IsStringRelation(RupayeRecord.getRemark(), remark)) {
                list.add(RupayeRecord);
            }
        }
        return list;
    }

    private void createTags() {
        saveTag(new Tag(-1, "Meal",                -1));
        saveTag(new Tag(-1, "Clothing & Footwear", 1));
        saveTag(new Tag(-1, "Home",                2));
        saveTag(new Tag(-1, "Traffic",             3));
        saveTag(new Tag(-1, "Vehicle Maintenance", 4));
        saveTag(new Tag(-1, "Book",                5));
        saveTag(new Tag(-1, "Hobby",               6));
        saveTag(new Tag(-1, "Internet",            7));
        saveTag(new Tag(-1, "Friend",              8));
        saveTag(new Tag(-1, "Education",           9));
        saveTag(new Tag(-1, "Entertainment",      10));
        saveTag(new Tag(-1, "Medical",            11));
        saveTag(new Tag(-1, "Insurance",          12));
        saveTag(new Tag(-1, "Donation",           13));
        saveTag(new Tag(-1, "Sport",              14));
        saveTag(new Tag(-1, "Snack",              15));
        saveTag(new Tag(-1, "Music",              16));
        saveTag(new Tag(-1, "Fund",               17));
        saveTag(new Tag(-1, "Drink",              18));
        saveTag(new Tag(-1, "Fruit",              19));
        saveTag(new Tag(-1, "Film",               20));
        saveTag(new Tag(-1, "Baby",               21));
        saveTag(new Tag(-1, "Partner",            22));
        saveTag(new Tag(-1, "Housing Loan",       23));
        saveTag(new Tag(-1, "Pet",                24));
        saveTag(new Tag(-1, "Telephone Bill",     25));
        saveTag(new Tag(-1, "Travel",             26));
        saveTag(new Tag(-1, "Lunch",              -2));
        saveTag(new Tag(-1, "Breakfast",          -3));
        saveTag(new Tag(-1, "MidnightSnack",      0));
        sortTAGS();
    }

    private void randomDataCreater() {

        Random random = new Random();

        List<RupayeRecord> createdRupayeRecords = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.set(2015, 0, 1, 0, 0, 0);
        c.add(Calendar.SECOND, 1);

        while (c.before(now)) {
            for (int i = 0; i < RANDOM_DATA_NUMBER_ON_EACH_DAY; i++) {
                Calendar r = (Calendar)c.clone();
                int hour = random.nextInt(24);
                int minute = random.nextInt(60);
                int second = random.nextInt(60);

                r.set(Calendar.HOUR_OF_DAY, hour);
                r.set(Calendar.MINUTE, minute);
                r.set(Calendar.SECOND, second);
                r.add(Calendar.SECOND, 0);

                int tag = random.nextInt(TAGS.size());
                int expense = random.nextInt(RANDOM_DATA_EXPENSE_ON_EACH_DAY) + 1;

                RupayeRecord RupayeRecord = new RupayeRecord();
                RupayeRecord.setCalendar(r);
                RupayeRecord.setMoney(expense);
                RupayeRecord.setTag(tag);
                RupayeRecord.setCurrency("RMB");
                RupayeRecord.setRemark("备注：这里显示备注~");

                createdRupayeRecords.add(RupayeRecord);
            }
            c.add(Calendar.DATE, 1);
        }

        Collections.sort(createdRupayeRecords, new Comparator<RupayeRecord>() {
            @Override
            public int compare(RupayeRecord lhs, RupayeRecord rhs) {
                if (lhs.getCalendar().before(rhs.getCalendar())) {
                    return -1;
                } else if (lhs.getCalendar().after(rhs.getCalendar())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (RupayeRecord RupayeRecord : createdRupayeRecords) {
            saveRecord(RupayeRecord);
        }
    }

    // Todo bug here
    private static void sortTAGS() {
        Collections.sort(TAGS, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                if (lhs.getWeight() != rhs.getWeight()) {
                    return Integer.valueOf(lhs.getWeight()).compareTo(rhs.getWeight());
                } else if (!lhs.getName().equals(rhs.getName())) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return Integer.valueOf(lhs.getId()).compareTo(rhs.getId());
                }
            }
        });
    }

}
