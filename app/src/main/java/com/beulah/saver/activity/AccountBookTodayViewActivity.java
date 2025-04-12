package com.beulah.saver.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bmob.BmobProFile;
import com.bmob.btp.callback.DeleteFileListener;
import com.bmob.btp.callback.DownloadListener;
import com.bmob.btp.callback.UploadListener;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.beulah.saver.BuildConfig;
import com.beulah.saver.R;
import com.beulah.saver.adapter.TodayViewFragmentAdapter;
import com.beulah.saver.model.Logo;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.model.SettingManager;
import com.beulah.saver.model.TaskManager;
import com.beulah.saver.model.UploadInfo;
import com.beulah.saver.model.User;
import com.beulah.saver.ui.CustomSliderView;
import com.beulah.saver.ui.MyQuery;
import com.beulah.saver.ui.RiseNumberTextView;
import com.beulah.saver.util.RupayeUtil;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class AccountBookTodayViewActivity extends AppCompatActivity {

    private static final String FILE_SEPARATOR = "/";
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + FILE_SEPARATOR +"Rupaye" + FILE_SEPARATOR;
    private static final String FILE_NAME = FILE_PATH + "Rupaye Database.db";

    private MaterialViewPager mViewPager;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private TodayViewFragmentAdapter todayModeAdapter = null;

    private Context mContext;

    private MaterialRippleLayout custom;
    private MaterialRippleLayout tags;
    private MaterialRippleLayout months;
    private MaterialRippleLayout list;
    private MaterialRippleLayout report;
    private MaterialRippleLayout sync;
    private MaterialRippleLayout settings;
    private MaterialRippleLayout help;
    private MaterialRippleLayout feedback;
    private MaterialRippleLayout about;

    private MaterialIconView syncIcon;

    private TextView userName;
    private TextView userEmail;

    private TextView title;

    private TextView monthExpenseTip;
    private RiseNumberTextView monthExpense;

    private CircleImageView profileImage;
    private SliderLayout mDemoSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_book_today_view);
        SuperToast.cancelAllSuperToasts();

        mContext = this;

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        userName = (TextView)findViewById(R.id.user_name);
        userEmail = (TextView)findViewById(R.id.user_email);
        userName.setTypeface(RupayeUtil.typefaceLatoRegular);
        userEmail.setTypeface(RupayeUtil.typefaceLatoLight);
        User user = BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class);
        if (user != null) {
            userName.setText(user.getUsername());
            userEmail.setText(user.getEmail());
        }

        setFonts();

        View view = mViewPager.getRootView();
        title = (TextView)view.findViewById(R.id.logo_white);
        title.setTypeface(RupayeUtil.typefaceLatoLight);
        title.setText(SettingManager.getInstance().getAccountBookName());

        mViewPager.getPagerTitleStrip().setTypeface(RupayeUtil.GetTypeface(), Typeface.NORMAL);

        setTitle("");

        toolbar = mViewPager.getToolbar();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        custom = (MaterialRippleLayout)mDrawer.findViewById(R.id.custom_layout);
        tags = (MaterialRippleLayout)mDrawer.findViewById(R.id.tag_layout);
        months = (MaterialRippleLayout)mDrawer.findViewById(R.id.month_layout);
        list = (MaterialRippleLayout)mDrawer.findViewById(R.id.list_layout);
        report = (MaterialRippleLayout)mDrawer.findViewById(R.id.report_layout);
        sync = (MaterialRippleLayout)mDrawer.findViewById(R.id.sync_layout);
        settings = (MaterialRippleLayout)mDrawer.findViewById(R.id.settings_layout);
        help = (MaterialRippleLayout)mDrawer.findViewById(R.id.help_layout);
        feedback = (MaterialRippleLayout)mDrawer.findViewById(R.id.feedback_layout);
        about = (MaterialRippleLayout)mDrawer.findViewById(R.id.about_layout);
        syncIcon = (MaterialIconView)mDrawer.findViewById(R.id.sync_icon);
        setIconEnable(syncIcon, SettingManager.getInstance().getLoggenOn());
        monthExpenseTip = (TextView)mDrawer.findViewById(R.id.month_expense_tip);
        monthExpenseTip.setTypeface(RupayeUtil.GetTypeface());
        monthExpense = (RiseNumberTextView)mDrawer.findViewById(R.id.month_expense);
        monthExpense.setTypeface(RupayeUtil.typefaceLatoLight);

        if (SettingManager.getInstance().getIsMonthLimit()) {
            monthExpenseTip.setVisibility(View.VISIBLE);
            monthExpense.setText("0");
        } else {
            monthExpenseTip.setVisibility(View.INVISIBLE);
            monthExpense.setVisibility(View.INVISIBLE);
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                monthExpense.setText("0");
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                monthExpense.withNumber(
                        RecordManager.getCurrentMonthExpense()).setDuration(500).start();
            }
        };
        mDrawer.setDrawerListener(mDrawerToggle);


        View logo = findViewById(R.id.logo_white);
        if (logo != null) {
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                }
            });
        }

        todayModeAdapter = new TodayViewFragmentAdapter(getSupportFragmentManager());
        mViewPager.getViewPager().setOffscreenPageLimit(todayModeAdapter.getCount());
        mViewPager.getViewPager().setAdapter(todayModeAdapter);
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                return HeaderDesign.fromColorAndDrawable(
                        RupayeUtil.GetTagColor(page - 2),
                        RupayeUtil.GetTagDrawable(-3)
                );
            }
        });

        setListeners();

        profileImage= (CircleImageView)mDrawer.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingManager.getInstance().getLoggenOn()) {
                    RupayeUtil.showToast(mContext, R.string.change_logo_tip);
                } else {
                    RupayeUtil.showToast(mContext, R.string.login_tip);
                }
            }
        });

        mDemoSlider = (SliderLayout)findViewById(R.id.slider);

        HashMap<String, Integer> urls = RupayeUtil.GetDrawerTopUrl();

        for(String name : urls.keySet()){
            CustomSliderView customSliderView = new CustomSliderView(this);
            // initialize a SliderLayout
            customSliderView
                    .image(urls.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            mDemoSlider.addSlider(customSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));

        loadLogo();

    }

    @Override
    protected void onStop() {
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MaterialViewPagerHelper.unregister(this);
    }

    private void loadRangeMode() {

        Log.d("Saver", "RANGE_MODE");

        Intent intent = new Intent(mContext, AccountBookCustomViewActivity.class);
        startActivity(intent);

    }

    private void loadTagMode() {

        Log.d("Saver", "TAG_MODE");

        Intent intent = new Intent(mContext, AccountBookTagViewActivity.class);
        startActivity(intent);

    }

    private void loadMonthMode() {

        Log.d("Saver", "MONTH_MODE");

        Intent intent = new Intent(mContext, AccountBookMonthViewActivity.class);
        startActivity(intent);

    }

    private void loadListMode() {

        Log.d("Saver", "LIST_MODE");

        Intent intent = new Intent(mContext, AccountBookListViewActivity.class);
        startActivity(intent);

    }

    private int syncSuccessNumber = 0;
    private int syncFailedNumber = 0;
    private int cloudRecordNumber = 0;
    private String cloudOldDatabaseUrl = null;
    private String cloudOldDatabaseFileName = null;
    private String uploadObjectId = null;
    MaterialDialog syncQueryDialog;
    MaterialDialog syncChooseDialog;
    MaterialDialog syncProgressDialog;
    private void sync() {
        if (!SettingManager.getInstance().getLoggenOn()) {
            RupayeUtil.showToast(mContext, R.string.login_tip);
        } else {
            syncSuccessNumber = 0;
            syncFailedNumber = 0;
            syncQueryDialog = new MaterialDialog.Builder(this)
                    .title(R.string.sync_querying_title)
                    .content(R.string.sync_querying_content)
                    .negativeText(R.string.cancel)
                    .progress(true, 0)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.NEGATIVE) {

                            }
                        }
                    })
                    .show();
            final User user = BmobUser
                    .getCurrentUser(RupayeApplication.getAppContext(), User.class);
            final MyQuery myQuery = new MyQuery();
            myQuery.setTask(++TaskManager.QUERY_UPDATE_TASK);
            myQuery.query = new BmobQuery<>();
            myQuery.query.addWhereEqualTo("userId", user.getObjectId());
            myQuery.query.setLimit(1);
            myQuery.query.findObjects(RupayeApplication.getAppContext(), new FindListener<UploadInfo>() {
                @Override
                public void onSuccess(List<UploadInfo> object) {
                    if (myQuery.getTask() != TaskManager.QUERY_UPDATE_TASK) return;
                    else {
                        syncQueryDialog.dismiss();
                        cloudRecordNumber = 0;
                        Calendar cal = null;
                        if (object.size() == 0) {

                        } else {
                            cloudRecordNumber = object.get(0).getRecordNumber();
                            cloudOldDatabaseUrl = object.get(0).getDatabaseUrl();
                            cloudOldDatabaseFileName = object.get(0).getFileName();
                            uploadObjectId = object.get(0).getObjectId();
                            cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                cal.setTime(sdf.parse(object.get(0).getUpdatedAt()));
                            } catch (ParseException p) {

                            }
                        }
                        String content
                                = RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_cloud_record_0)
                                + cloudRecordNumber
                                + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_cloud_record_1)
                                + (cal == null ? RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_cloud_time_2) : RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_cloud_time_0) + RupayeUtil.GetCalendarString(RupayeApplication.getAppContext(), cal) + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_cloud_time_1))
                                + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_mobile_record_0)
                                + RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size()
                                + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_mobile_record_1)
                                + (SettingManager.getInstance().getRecentlySyncTime() == null ? RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_mobile_time_2) : RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_mobile_time_0) + RupayeUtil.GetCalendarString(RupayeApplication.getAppContext(), SettingManager.getInstance().getRecentlySyncTime()) + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_info_mobile_time_1))
                                + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sync_choose_content);
                        syncChooseDialog = new MaterialDialog.Builder(mContext)
                                .title(R.string.sync_choose_title)
                                .content(content)
                                .positiveText(R.string.sync_to_cloud)
                                .negativeText(R.string.sync_to_mobile)
                                .neutralText(R.string.cancel)
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        syncChooseDialog.dismiss();
                                        if (which == DialogAction.POSITIVE) {
                                            // sync to cloud
                                            String subContent = "";
                                            if (RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size() == 0) {
                                                subContent = RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.mobile_record_empty);
                                                new MaterialDialog.Builder(mContext)
                                                        .title(R.string.sync)
                                                        .content(subContent)
                                                        .positiveText(R.string.ok_1)
                                                        .show();
                                                return;
                                            } else {
                                                subContent
                                                        = RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sure_to_cloud_0)
                                                        + RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size()
                                                        + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sure_to_cloud_1);
                                            }
                                            new MaterialDialog.Builder(mContext)
                                                    .title(R.string.sync)
                                                    .content(subContent)
                                                    .positiveText(R.string.ok_1)
                                                    .negativeText(R.string.cancel)
                                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            if (which == DialogAction.POSITIVE) {
                                                                syncProgressDialog = new MaterialDialog.Builder(mContext)
                                                                        .title(R.string.syncing)
                                                                        .content(RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.uploading_0) + "1" + RupayeUtil.GetString(mContext, R.string.uploading_1))
                                                                        .progress(false, RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size(), true)
                                                                        .cancelable(false)
                                                                        .show();
                                                                final String databasePath = RupayeUtil.GetRecordDatabasePath(RupayeApplication.getAppContext());
//                                                                final BmobFile bmobFile = new BmobFile(new File(databasePath));
//                                                                bmobFile.uploadblock(mContext, new UploadFileListener() {
//
//                                                                    @Override
//                                                                    public void onSuccess() {
//                                                                        if (BuildConfig.DEBUG) {
//                                                                            Log.d("Rupaye", "Upload successfully fileName: " + databasePath);
//                                                                            Log.d("Rupaye", "Upload successfully url: " + bmobFile.getFileUrl(mContext));
//                                                                        }
//                                                                        // the new database is uploaded successfully
//                                                                        // delete the old database(if there is)
//                                                                        if (cloudOldDatabaseUrl != null) {
//                                                                            deleteOldDatabaseOnCloud(cloudOldDatabaseUrl);
//                                                                        }
//                                                                        // update the UploadInfo record for the new url
//                                                                        if (uploadObjectId == null) {
//                                                                            // first time
//                                                                            UploadInfo uploadInfo = new UploadInfo();
//                                                                            uploadInfo.setUserId(user.getObjectId());
//                                                                            uploadInfo.setRecordNumber(RecordManager.getInstance(mContext).RECORDS.size());
//                                                                            uploadInfo.setDatabaseUrl(bmobFile.getFileUrl(mContext));
//                                                                            uploadInfo.save(mContext, new SaveListener() {
//                                                                                @Override
//                                                                                public void onSuccess() {
//                                                                                    // upload successfully
//                                                                                    syncProgressDialog.dismiss();
//                                                                                    new MaterialDialog.Builder(mContext)
//                                                                                            .title(R.string.sync_completely_title)
//                                                                                            .content(RecordManager.getInstance(mContext).RECORDS.size() + RupayeUtil.GetString(mContext, R.string.uploading_fail_1))
//                                                                                            .positiveText(R.string.ok_1)
//                                                                                            .show();
//                                                                                }
//                                                                                @Override
//                                                                                public void onFailure(int code, String arg0) {
//                                                                                    // 添加失败
//                                                                                }
//                                                                            });
//                                                                        } else {
//                                                                            UploadInfo uploadInfo = new UploadInfo();
//                                                                            uploadInfo.setUserId(user.getObjectId());
//                                                                            uploadInfo.setRecordNumber(RecordManager.getInstance(mContext).RECORDS.size());
//                                                                            uploadInfo.setDatabaseUrl(bmobFile.getFileUrl(mContext));
//                                                                            uploadInfo.update(mContext, uploadObjectId, new UpdateListener() {
//                                                                                @Override
//                                                                                public void onSuccess() {
//                                                                                    // upload successfully
//                                                                                    syncProgressDialog.dismiss();
//                                                                                    new MaterialDialog.Builder(mContext)
//                                                                                            .title(R.string.sync_completely_title)
//                                                                                            .content(RecordManager.getInstance(mContext).RECORDS.size() + RupayeUtil.GetString(mContext, R.string.uploading_fail_1))
//                                                                                            .positiveText(R.string.ok_1)
//                                                                                            .show();
//                                                                                }
//                                                                                @Override
//                                                                                public void onFailure(int code, String msg) {
//                                                                                    // upload failed
//                                                                                    Log.i("bmob","更新失败："+msg);
//                                                                                }
//                                                                            });
//                                                                        }
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onProgress(Integer value) {
//                                                                        syncProgressDialog.setProgress(value);
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onFailure(int code, String msg) {
//                                                                        // upload failed
//                                                                        if (BuildConfig.DEBUG) Log.d("Rupaye", "Upload database failed " + code + " " + msg);
//                                                                        syncProgressDialog.dismiss();
//                                                                        new MaterialDialog.Builder(mContext)
//                                                                                .title(R.string.sync_failed)
//                                                                                .content(R.string.uploading_fail_0)
//                                                                                .positiveText(R.string.ok_1)
//                                                                                .show();
//                                                                    }
//                                                                });
                                                                BmobProFile.getInstance(RupayeApplication.getAppContext()).upload(databasePath, new UploadListener() {
                                                                    @Override
                                                                    public void onSuccess(String fileName, String url, BmobFile file) {
                                                                        RupayeUtil.deleteBmobUploadCach(RupayeApplication.getAppContext());
                                                                        if (BuildConfig.DEBUG) {
                                                                            Log.d("Rupaye", "Upload successfully fileName: " + fileName);
                                                                            Log.d("Rupaye", "Upload successfully url: " + url);
                                                                        }
                                                                        // the new database is uploaded successfully
                                                                        // delete the old database(if there is)
                                                                        if (cloudOldDatabaseFileName != null) {
                                                                            deleteOldDatabaseOnCloud(cloudOldDatabaseFileName);
                                                                        }
                                                                        // update the UploadInfo record for the new url
                                                                        UploadInfo uploadInfo = new UploadInfo();
                                                                        uploadInfo.setUserId(user.getObjectId());
                                                                        uploadInfo.setRecordNumber(RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size());
                                                                        uploadInfo.setDatabaseUrl(file.getFileUrl(RupayeApplication.getAppContext()));
                                                                        uploadInfo.setFileName(fileName);
                                                                        if (uploadObjectId == null) {
                                                                            // insert
                                                                            uploadInfo.save(RupayeApplication.getAppContext(), new SaveListener() {
                                                                                @Override
                                                                                public void onSuccess() {
                                                                                    // upload successfully
                                                                                    syncProgressDialog.dismiss();
                                                                                    new MaterialDialog.Builder(mContext)
                                                                                            .title(R.string.sync_completely_title)
                                                                                            .content(RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size() + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.uploading_fail_1))
                                                                                            .positiveText(R.string.ok_1)
                                                                                            .cancelable(false)
                                                                                            .show();
                                                                                }
                                                                                @Override
                                                                                public void onFailure(int code, String arg0) {
                                                                                    uploadFailed(code, arg0);
                                                                                }
                                                                            });
                                                                        } else {
                                                                            // update
                                                                            uploadInfo.update(RupayeApplication.getAppContext(), uploadObjectId, new UpdateListener() {
                                                                                @Override
                                                                                public void onSuccess() {
                                                                                    // upload successfully
                                                                                    syncProgressDialog.dismiss();
                                                                                    new MaterialDialog.Builder(mContext)
                                                                                            .title(R.string.sync_completely_title)
                                                                                            .content(RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size() + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.uploading_fail_1))
                                                                                            .positiveText(R.string.ok_1)
                                                                                            .cancelable(false)
                                                                                            .show();
                                                                                }
                                                                                @Override
                                                                                public void onFailure(int code, String msg) {
                                                                                    uploadFailed(code, msg);
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                    @Override
                                                                    public void onProgress(int progress) {
                                                                        syncProgressDialog.setProgress((int)(progress * 1.0 / 100 * RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size()));
                                                                    }

                                                                    @Override
                                                                    public void onError(int statuscode, String errormsg) {
                                                                        // upload failed
                                                                        uploadFailed(statuscode, errormsg);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    })
                                                    .show();
                                        } else if (which == DialogAction.NEGATIVE) {
                                            // sync to mobile
                                            String subContent = "";
                                            if (cloudRecordNumber == 0) {
                                                subContent = RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.cloud_record_empty);
                                                new MaterialDialog.Builder(mContext)
                                                        .title(R.string.sync)
                                                        .content(subContent)
                                                        .positiveText(R.string.ok_1)
                                                        .show();
                                                return;
                                            } else {
                                                subContent
                                                        = RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sure_to_mobile_0)
                                                        + cloudRecordNumber
                                                        + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.sure_to_mobile_1);
                                            }
                                            new MaterialDialog.Builder(mContext)
                                                    .title(R.string.sync)
                                                    .content(subContent)
                                                    .positiveText(R.string.ok_1)
                                                    .negativeText(R.string.cancel)
                                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            if (which == DialogAction.POSITIVE) {
                                                                syncProgressDialog = new MaterialDialog.Builder(mContext)
                                                                        .title(R.string.syncing)
                                                                        .content(RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.downloading_0) + "1" + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.downloading_1))
                                                                        .progress(false, cloudRecordNumber, true)
                                                                        .cancelable(false)
                                                                        .show();
                                                                // download the database file to mobile
                                                                BmobProFile.getInstance(RupayeApplication.getAppContext()).download(cloudOldDatabaseFileName, new DownloadListener() {
                                                                    @Override
                                                                    public void onSuccess(String fullPath) {
                                                                        // download completely
                                                                        // delete the original database in mobile
                                                                        // copy the new database to mobile
                                                                        try {
                                                                            Log.d("Rupaye", "Download successfully " + fullPath);
                                                                            syncProgressDialog.setContent(R.string.sync_completely_content);
                                                                            byte[] buffer = new byte[1024];
                                                                            File file = new File(fullPath);
                                                                            InputStream inputStream = new FileInputStream(file);
                                                                            String outFileNameString = RupayeUtil.GetRecordDatabasePath(RupayeApplication.getAppContext());
                                                                            OutputStream outputStream = new FileOutputStream(outFileNameString);
                                                                            int length;
                                                                            while ((length = inputStream.read(buffer)) > 0) {
                                                                                outputStream.write(buffer, 0, length);
                                                                            }
                                                                            Log.d("Rupaye", "Download successfully copy completely");
                                                                            outputStream.flush();
                                                                            outputStream.close();
                                                                            inputStream.close();
                                                                            file.delete();
                                                                            Log.d("Rupaye", "Download successfully delete completely");
                                                                            // refresh data
                                                                            RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.clear();
                                                                            RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS = null;
                                                                            RecordManager.getInstance(RupayeApplication.getAppContext());
                                                                            todayModeAdapter.notifyDataSetChanged();
                                                                            Log.d("Rupaye", "Download successfully refresh completely");
                                                                            syncProgressDialog.dismiss();
                                                                            new MaterialDialog.Builder(mContext)
                                                                                    .title(R.string.sync_completely_title)
                                                                                    .content(cloudRecordNumber + RupayeUtil.GetString(RupayeApplication.getAppContext(), R.string.downloading_fail_1))
                                                                                    .positiveText(R.string.ok_1)
                                                                                    .cancelable(false)
                                                                                    .show();
                                                                        } catch (IOException i) {
                                                                            i.printStackTrace();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onProgress(String localPath, int percent) {
                                                                        syncProgressDialog.setProgress((int) (((float) percent / 100) * RecordManager.getInstance(RupayeApplication.getAppContext()).RECORDS.size()));
                                                                    }

                                                                    @Override
                                                                    public void onError(int statuscode, String errormsg) {
                                                                        downloadFailed(statuscode, errormsg);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    })
                                                    .show();
                                        } else {
                                        }
                                    }
                                })
                                .show();
                    }
                }
                @Override
                public void onError(int code, String msg) {
                    syncQueryDialog.dismiss();
                    if (BuildConfig.DEBUG) Log.d("Rupaye", "Query: " + msg);
                    if (syncQueryDialog != null) syncQueryDialog.dismiss();
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.sync_querying_fail_title)
                            .content(R.string.sync_querying_fail_content)
                            .positiveText(R.string.ok_1)
                            .show();
                }
            });
        }
    }

    private void deleteOldDatabaseOnCloud(final String fileName) {
        BmobProFile.getInstance(RupayeApplication.getAppContext()).deleteFile(fileName, new DeleteFileListener() {
            @Override
            public void onError(int errorcode, String errormsg) {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Delete old cloud database failed " + cloudOldDatabaseUrl);
            }
            @Override
            public void onSuccess() {
                if (BuildConfig.DEBUG) Log.d("Rupaye", "Delete old cloud database successfully " + cloudOldDatabaseUrl);
            }
        });
    }

    private void uploadFailed(int code, String msg) {
        // upload failed
        if (BuildConfig.DEBUG) Log.d("Rupaye", "Upload database failed " + code + " " + msg);
        syncProgressDialog.dismiss();
        new MaterialDialog.Builder(mContext)
                .title(R.string.sync_failed)
                .content(R.string.uploading_fail_0)
                .positiveText(R.string.ok_1)
                .cancelable(false)
                .show();
    }

    private void downloadFailed(int code, String msg) {
        // upload failed
        if (BuildConfig.DEBUG) Log.d("Rupaye", "Download database failed " + code + " " + msg);
        syncProgressDialog.dismiss();
        new MaterialDialog.Builder(mContext)
                .title(R.string.sync_failed)
                .content(R.string.downloading_fail_0)
                .positiveText(R.string.ok_1)
                .cancelable(false)
                .show();
    }

    private SaveListener uploadCounter = new SaveListener() {
        @Override
        public void onSuccess() {
            syncSuccessNumber++;
            syncProgressDialog.incrementProgress(1);
            if (syncSuccessNumber == RecordManager.getInstance(mContext).RECORDS.size()) {
                syncProgressDialog.setContent(R.string.sync_completely_content);
            } else {
                syncProgressDialog.setContent(RupayeUtil.GetString(mContext, R.string.uploading_0) + (syncSuccessNumber + 1) + RupayeUtil.GetString(mContext, R.string.uploading_1));
            }
            if (syncSuccessNumber + syncFailedNumber == RecordManager.getInstance(mContext).RECORDS.size()) {
                syncProgressDialog.dismiss();
                new MaterialDialog.Builder(mContext)
                        .title(R.string.sync_completely_title)
                        .content(syncSuccessNumber + RupayeUtil.GetString(mContext, R.string.uploading_fail_1))
                        .positiveText(R.string.ok_1)
                        .show();
            }
        }
        @Override
        public void onFailure(int code, String arg0) {
            syncFailedNumber++;
            syncProgressDialog.incrementProgress(1);
            if (syncSuccessNumber + syncFailedNumber == RecordManager.getInstance(mContext).RECORDS.size()) {
                syncProgressDialog.dismiss();
                new MaterialDialog.Builder(mContext)
                        .title(R.string.sync_completely_title)
                        .content(syncSuccessNumber + RupayeUtil.GetString(mContext, R.string.uploading_fail_1))
                        .positiveText(R.string.ok_1)
                        .show();
            }
        }
    };

    private void loadSettings() {

        Log.d("Saver", "SETTINGS");

        Intent intent = new Intent(mContext, AccountBookSettingActivity.class);
        startActivity(intent);

    }

    @Override
    public void onResume() {

        if (mDemoSlider != null) mDemoSlider.startAutoCycle();

        super.onResume();

        if (SettingManager.getInstance().getTodayViewPieShouldChange()) {
            todayModeAdapter.notifyDataSetChanged();
            SettingManager.getInstance().setTodayViewPieShouldChange(Boolean.FALSE);
        }

        if (SettingManager.getInstance().getTodayViewTitleShouldChange()) {
            title.setText(SettingManager.getInstance().getAccountBookName());
            SettingManager.getInstance().setTodayViewTitleShouldChange(false);
        }

        if (SettingManager.getInstance().getRecordIsUpdated()) {
            todayModeAdapter.notifyDataSetChanged();
            SettingManager.getInstance().setRecordIsUpdated(false);
        }

        if (SettingManager.getInstance().getTodayViewMonthExpenseShouldChange()) {
            if (SettingManager.getInstance().getIsMonthLimit()) {
                monthExpenseTip.setVisibility(View.VISIBLE);
                monthExpense.withNumber(
                        RecordManager.getCurrentMonthExpense()).setDuration(500).start();
            } else {
                monthExpenseTip.setVisibility(View.INVISIBLE);
                monthExpense.setVisibility(View.INVISIBLE);
            }
        }

        if (SettingManager.getInstance().getTodayViewLogoShouldChange()) {
            loadLogo();
            SettingManager.getInstance().setTodayViewLogoShouldChange(false);
        }

        if (SettingManager.getInstance().getTodayViewInfoShouldChange()) {
            setIconEnable(syncIcon, SettingManager.getInstance().getLoggenOn());
            User user = BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class);
            if (user != null) {
                userName.setText(user.getUsername());
                userEmail.setText(user.getEmail());
                loadLogo();
            } else {
                userName.setText("");
                userEmail.setText("");
                loadLogo();
            }
            SettingManager.getInstance().setTodayViewInfoShouldChange(false);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void setFonts() {
        userName.setTypeface(RupayeUtil.typefaceLatoRegular);
        userEmail.setTypeface(RupayeUtil.typefaceLatoLight);
        ((TextView)findViewById(R.id.custom_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.tag_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.month_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.list_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.report_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.sync_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.settings_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.help_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.feedback_text)).setTypeface(RupayeUtil.GetTypeface());
        ((TextView)findViewById(R.id.about_text)).setTypeface(RupayeUtil.GetTypeface());
    }

    private void setListeners() {
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRangeMode();
            }
        });
        tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTagMode();
            }
        });
        months.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMonthMode();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSettings();
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadListMode();
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AccountBookReportViewActivity.class));
            }
        });
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, HelpActivity.class));
            }
        });
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, FeedbackActivity.class));
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AboutActivity.class));
            }
        });
    }

    private void loadLogo() {
        User user = BmobUser.getCurrentUser(RupayeApplication.getAppContext(), User.class);
        if (user != null) {
            try {
                File logoFile = new File(RupayeApplication.getAppContext().getFilesDir() + RupayeUtil.LOGO_NAME);
                if (!logoFile.exists()) {
                    // the local logo file is missed
                    // try to get from the server
                    BmobQuery<Logo> bmobQuery = new BmobQuery();
                    Log.d("Rupaye", user.getLogoObjectId());
                    bmobQuery.addWhereEqualTo("objectId", user.getLogoObjectId());
                    bmobQuery.findObjects(RupayeApplication.getAppContext()
                            , new FindListener<Logo>() {
                        @Override
                        public void onSuccess(List<Logo> object) {
                            // there has been an old logo in the server/////////////////////////////////////////////////////////
                            String url = object.get(0).getFile().getFileUrl(RupayeApplication.getAppContext());
                            if (BuildConfig.DEBUG) Log.d("Rupaye", "Logo in server: " + url);
                            Ion.with(RupayeApplication.getAppContext()).load(url)
                                    .write(new File(RupayeApplication.getAppContext().getFilesDir()
                                            + RupayeUtil.LOGO_NAME))
                                    .setCallback(new FutureCallback<File>() {
                                        @Override
                                        public void onCompleted(Exception e, File file) {
                                            profileImage.setImageBitmap(BitmapFactory.decodeFile(
                                                    RupayeApplication.getAppContext().getFilesDir()
                                                            + RupayeUtil.LOGO_NAME));
                                        }
                                    });
                        }
                        @Override
                        public void onError(int code, String msg) {
                            // the picture is lost
                            if (BuildConfig.DEBUG) Log.d("Rupaye", "Can't find the old logo in server.");
                        }
                    });
                } else {
                    // the user logo is in the storage
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(logoFile));
                    profileImage.setImageBitmap(b);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // use the default logo
            profileImage.setImageResource(R.drawable.default_user_logo);
        }
    }

    private void setIconEnable(MaterialIconView icon, boolean enable) {
        if (enable) icon.setColor(mContext.getResources().getColor(R.color.my_blue));
        else icon.setColor(mContext.getResources().getColor(R.color.my_gray));
    }
}
