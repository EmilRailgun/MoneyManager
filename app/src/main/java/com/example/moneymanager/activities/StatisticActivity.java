package com.example.moneymanager.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.moneymanager.R;
import com.example.moneymanager.constant.SharedPrefConstant;
import com.example.moneymanager.methods.SharedMethods;
import com.github.dewinjm.monthyearpicker.MonthFormat;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatisticActivity extends AppCompatActivity{
    BottomNavigationView bnv_menu;
    FloatingActionButton fab_add_transaction;
    TextView tv_first_surplus, tv_latest_surplus, tv_time_picker_print,
            tv_revenue, tv_expenditure,
            tv_wallet_name, tv_wallet_money;
    LinearLayout ll_time_picker;
    MonthYearPickerDialogFragment dialogFragment;

    PieChart pc_revenue, pc_expenditure;

    //T???o m???ng ch??a gi?? tr??? giao d???ch
    int expenditure_value[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    int revenue_value[] = {0,0,0,0,0,0};
    //T???o m???ng ch??a gi?? tr??? m??u giao d???ch
    int expenditure_color[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    int revenue_color[] = {0,0,0,0,0,0};
    //T???o m???ng ch??a t??n giao d???ch
    String expenditure_title[] = {"","","","","","","","","","","","","","","","","","","","",""};
    String revenue_title[] = {"","","","","",""};

    int yearSelected;
    int monthSelected;
    int revenue, expenditure;

    private void getViews(){
        bnv_menu = findViewById(R.id.statistic_bnv_menu);
        fab_add_transaction = findViewById(R.id.statistic_fab_add_transaction);
        tv_first_surplus = findViewById(R.id.statistic_tv_first_surplus);
        tv_latest_surplus = findViewById(R.id.statistic_tv_latest_surplus);
        ll_time_picker = findViewById(R.id.statistic_first_ll_time_picker);
        tv_time_picker_print = findViewById(R.id.statistic_first_tv_time_picker_print);
        tv_revenue = findViewById(R.id.statistic_tv_revenue);
        tv_expenditure = findViewById(R.id.statistic_tv_expenditure);
        pc_revenue = findViewById(R.id.statistic_pc_revenue);
        pc_expenditure = findViewById(R.id.statistic_pc_expenditure);
        tv_wallet_name = findViewById(R.id.statistic_tv_wallet_name);
        tv_wallet_money = findViewById(R.id.statistic_tv_money);

    }

    private void displayUserInformation(){
        //L???y file ng?????i d??ng ??ang ????ng nh???p
        SharedPreferences sharedPreferenceSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        //Set t??n v??
        String walletName = sharedPreferenceSigningIn.getString(SharedPrefConstant.SIGNING_IN_WALLET_NAME, "");
        tv_wallet_name.setText(walletName);

        //Set s??? ti???n trong v??
        String username = sharedPreferenceSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        int money = sharedPreferencesTransaction.getInt(SharedPrefConstant.WALLET_MONEY, 0);

        //Set m??u ti???n
        if(money > 0)
            tv_wallet_money.setTextColor(getResources().getColor(R.color.green_main));
        else
            tv_wallet_money.setTextColor(getResources().getColor(R.color.red_form_error));

        //Format ti???n t??? 100000 th??nh 100.000 ??
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        String formattedMoney = decimalFormat.format(money);

        tv_wallet_money.setText(formattedMoney + " ??");
    }

    private void setDateLitener() {
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);
        //?????t ti??u ?????
        String customTitle = "Ch???n th???i gian";
        //?????t ng??n ng???
        Locale locale = new Locale("vi");
        //?????t d???ng hi???n th??? th??ng
        MonthFormat monthFormat = MonthFormat.LONG;
        //T???o l???ch
        dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected, customTitle, locale, monthFormat);
        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                tv_time_picker_print.setText((monthOfYear+1) + "/ " +year);
                try {
                    //HI???n th??? chart theo th??ng
                    setRevenuePieChartByDate(year, monthOfYear);
                    setExpenditurePieChartByDate(year, monthOfYear);
                    //Hi???n th??? th??ng tin t???ng qu??t theo th??ng
                    getRevenueExpenditureByDate(year, monthOfYear);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setEventListener(){
        //H??m n??y ???????c g???i m???i khi c?? item tr??n menu ???????c ???n
        bnv_menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item_wallet:
                        Intent walletIntent = new Intent(StatisticActivity.this, WalletActivity.class);
                        startActivity(walletIntent);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.item_plan:
                        Intent planIntent = new Intent(StatisticActivity.this, PlanActivity.class);
                        startActivity(planIntent);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.item_account:
                        Intent accountIntent = new Intent(StatisticActivity.this, AccountActivity.class);
                        startActivity(accountIntent);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                }
                return false;
            }
        });

        fab_add_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addTransactionIntent = new Intent(StatisticActivity.this, AddTransactionActivity.class);
                startActivity(addTransactionIntent);
                overridePendingTransition(0,0);
            }
        });

        ll_time_picker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialogFragment.show(getSupportFragmentManager(), null);
            }
        });
    }

    private void getWalletMoney(){
        //L???y th??ng tin ng?????i ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN,MODE_PRIVATE);
        String userName = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(userName,MODE_PRIVATE);
        //L???y s??? d?? ?????u
        int WalletMoneyDefault = sharedPreferencesTransaction.getInt(SharedPrefConstant.WALLET_MONEY_DEFAULT, 0);
        //L???y s??? d?? cu???i
        int WalletMoney = sharedPreferencesTransaction.getInt(SharedPrefConstant.WALLET_MONEY, 0);
        tv_first_surplus.setText(String.valueOf(WalletMoneyDefault));
        tv_latest_surplus.setText(String.valueOf(WalletMoney));
    }

    private void getRevenueExpenditure () {
        //L???y th??ng tin ng?????i d??ng ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);

        //L???y t???ng s??? giao d???ch
        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            //L???y ID v?? s??? ti???n c???a giao d???ch
            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            if (transactionCategoryId < 20 || transactionCategoryId == 26 || transactionCategoryId == 27) {
                expenditure = expenditure + transactionMoney;
            } else if (transactionCategoryId >= 20) {
                revenue = revenue + transactionMoney;
            }

            //?????i sang ?????nh d???ng 000,000
            DecimalFormat decimalFormat = new DecimalFormat("###,###");
            String revenue_money = decimalFormat.format(revenue);
            String expenditure_money = decimalFormat.format(expenditure);
            tv_revenue.setText(revenue_money);
            tv_expenditure.setText(expenditure_money);
        }
    }

    private void getRevenueExpenditureByDate (int year, int monthOfYear) throws ParseException {
        //L???y th??ng tin ng?????i d??ng ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        //L???y t???ng s??? giao d???ch
        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            String transactionDate = sharedPreferencesTransaction.getString(String.format("%s_%d", SharedPrefConstant.TRANSACTION_DATE, i), null);
            Date date =new SimpleDateFormat("dd/MM/yyyy").parse(transactionDate);
            expenditure = 0 ;
            revenue = 0 ;
            if (date.getMonth() == monthOfYear && date.getYear() == year) {
                if (transactionCategoryId < 20 || transactionCategoryId == 26 || transactionCategoryId == 27) {
                    expenditure = expenditure + transactionMoney;
                } else if (transactionCategoryId >= 20) {
                    revenue = revenue + transactionMoney;
                }
            }
            //?????i sang ?????nh d???ng 000,000
            DecimalFormat decimalFormat = new DecimalFormat("###,###");
            String revenue_money = decimalFormat.format(revenue);
            String expenditure_money = decimalFormat.format(expenditure);
            tv_revenue.setText(revenue_money);
            tv_expenditure.setText(expenditure_money);
        }
    }

    //Truy???n v??o id v?? gi?? tr??? giao d???ch,
    // D???a theo id ????? l??u  gi?? tr???, m??u, ti??u ????? t????ng ???ng
    // v??o m???ng tr??n
    public void setRevenueItemValueColor(int id, int value) {
        switch (id) {
            case 1: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m1);
                expenditure_title[id-1] = "??n u???ng";
                break;
            case 2: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m2);
                expenditure_title[id-1] = "Di chuy???n";
                break;
            case 3: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m3);
                expenditure_title[id-1] = "H??a ????n";
                break;
            case 4: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m4);
                expenditure_title[id-1] = "Trang tr??, s???a nh??";
                break;
            case 5: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m5);
                expenditure_title[id-1] = "B???o d?????ng xe";
                break;
            case 6: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m6);
                expenditure_title[id-1] = "S???c kh???e";
                break;
            case 7: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m7);
                expenditure_title[id-1] = "H???c t???p";
                break;
            case 8: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m8);
                expenditure_title[id-1] = "????? gia d???ng";
                break;
            case 9: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m9);
                expenditure_title[id-1] = "B???o hi???m";
                break;
            case 10: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m10);
                expenditure_title[id-1] = "????? d??ng c?? nh??n";
                break;
            case 11: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m11);
                expenditure_title[id-1] = "V???t nu??i";
                break;
            case 12: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m12);
                expenditure_title[id-1] = "D???ch v??? gia ????nh";
                break;
            case 13: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m13);
                expenditure_title[id-1] = "Th??? thao";
                break;
            case 14: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m14);
                expenditure_title[id-1] = "L??m ?????p";
                break;
            case 15: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m15);
                expenditure_title[id-1] = "D???ch v??? tr?????c tuy???n";
                break;
            case 16: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m16);
                expenditure_title[id-1] = "Gi???i tr??";
                break;
            case 17: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m17);
                expenditure_title[id-1] = "Tr??? l??i";
                break;
            case 18: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m18);
                expenditure_title[id-1] = "Chuy???n ti???n ??i";
                break;
            case 19: expenditure_value[id-1] = expenditure_value[id-1] + value;
                expenditure_color[id-1] = getColor(R.color.m19);
                expenditure_title[id-1] = "C??c kho???n chi kh??c";
                break;
            case 20: revenue_value[0] = revenue_value[0] + value;
                revenue_color[0] = getColor(R.color.m20);
                revenue_title[0] = "L????ng";
                break;
            case 21: revenue_value[1] = revenue_value[1] + value;
                revenue_color[1] = getColor(R.color.m21);
                revenue_title[1] = "Thu l??i";
                break;
            case 22: revenue_value[2] = revenue_value[2] + value;
                revenue_color[2] = getColor(R.color.m22);
                revenue_title[2] = "Chuy???n ti???n ?????n";
                break;
            case 23: revenue_value[3] = revenue_value[3] + value;
                revenue_color[3] = getColor(R.color.m23);
                revenue_title[3] = "C??c kho???n thu kh??c";
                break;
            case 24: revenue_value[4] = revenue_value[3] + value;
                revenue_color[4] = getColor(R.color.m24);
                revenue_title[4] = "Thu n???";
                break;
            case 25: revenue_value[5] = revenue_value[5] + value;
                revenue_color[5] = getColor(R.color.m25);
                revenue_title[5] = "C??c kho???n thu kh??c";
                break;
            case 26: expenditure_value[19] = expenditure_value[19] + value;
                expenditure_color[19] = getColor(R.color.m26);
                expenditure_title[19] = "Cho vay";
                break;
            case 27: expenditure_value[20] = expenditure_value[20] + value;
                expenditure_color[20] = getColor(R.color.m27);
                expenditure_title[20] = "Tr??? n???";
                break;
        }
    }

    public void setRevenuePieChart () {
        pc_revenue.setUsePercentValues(true);
        pc_revenue.getDescription().setEnabled(false);
        pc_revenue.setExtraOffsets(5, 10, 5, 5);

        pc_revenue.setDragDecelerationFrictionCoef(0.95f);

        pc_revenue.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        //V??ng tr??n gi???a bi???u ?????
        pc_revenue.setDrawHoleEnabled(true);
        pc_revenue.setHoleColor(Color.WHITE);
        //V??ng tr??n m??? gi???a bi???u ?????
        pc_revenue.setTransparentCircleColor(Color.WHITE);
        pc_revenue.setTransparentCircleAlpha(110);

        pc_revenue.setHoleRadius(40f); //k??ch th?????c h??nh tr??n
        pc_revenue.setTransparentCircleRadius(61f); // k??ch th?????c h??nh tr??n m???

        pc_revenue.setRotationAngle(0); // g??c nghi???ng c???a bi???u ?????

        pc_revenue.setRotationEnabled(false); // V?? hi???u h??a xoay
        pc_revenue.setHighlightPerTapEnabled(true); // ?????t ph??ng to khi ch???n
        pc_revenue.animateXY(1000,2000); // Animation hi???n th???

        pc_revenue.setDrawEntryLabels(false); // V?? hi???u h??a m?? t??? trong bi???u ?????

        //V?? hi???u h??a ph???n ch?? th??ch
        Legend lg = pc_revenue.getLegend();
        lg.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        lg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        lg.setOrientation(Legend.LegendOrientation.VERTICAL);
        lg.setDrawInside(false);
        lg.setEnabled(false);

        //L???y th??ng tin ng?????i d??ng ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        //L???y s??? giao d???ch
        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            //L???y id v?? gi?? tr??? c???a giao d???ch
            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            if (transactionCategoryId >= 20) {
                //L??u th??ng tin giao d???ch v??o m???ng d???a theo id
                setRevenueItemValueColor(transactionCategoryId, transactionMoney);
            }
        }

        //T???o list d??? li???u v?? m??u cho chart
        ArrayList<PieEntry> revenue_entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (revenue_value[i] != 0) {
                //Th??m th??ng tin v??o Arraylisst
                revenue_entries.add(new PieEntry(revenue_value[i],revenue_title[i]));
                colors.add(revenue_color[i]);
            }
        }

        //T???o d??? li???u cho piechart
        PieDataSet dataSet = new PieDataSet(revenue_entries, "");
        dataSet.setSliceSpace(0f); //Kho???ng c??ch gi???a t???ng mi???ng
        dataSet.setSelectionShift(5f); // K??ch c??? t??ng khi ??c ch???n
        dataSet.setValueTextSize(15f); // K??ch c??? ch??? gi?? tr???
        dataSet.setColors(colors); // M??u cho mi???ng
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); //??ua ph???n m?? t??? gi?? tr??? ra ngo??i

        PieData revenue_pie_data = new PieData(dataSet); //l??u gi??? li???u

        pc_revenue.setData(revenue_pie_data); // truy???n d??? li???u v??o chart
        pc_revenue.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                PieEntry pe = (PieEntry) e;
                //Hi???n th??ng tin khi nh???n v??o t???ng mi???ng qua toast
                Toast.makeText(StatisticActivity.this, "Nh??m "
                        + pe.getLabel()
                        + " c?? t???ng "
                        + e.getY()
                        + "??", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected()
            {

            }
        });
        //Ki???m tra n???u d??? li???u tr???ng th?? th??ng b??o
        if(revenue_entries.isEmpty()) {
            pc_revenue.setCenterText("D??? li???u tr???ng");
        } else {
            pc_revenue.setCenterText("");
        }
    }

    private void setRevenuePieChartByDate (int year, int monthOfYear) throws ParseException {
        pc_revenue.setUsePercentValues(true);
        pc_revenue.getDescription().setEnabled(false);
        pc_revenue.setExtraOffsets(5, 10, 5, 5);

        pc_revenue.setDragDecelerationFrictionCoef(0.95f);

        pc_revenue.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        //V??ng tr??n gi???a bi???u ?????
        pc_revenue.setDrawHoleEnabled(true);
        pc_revenue.setHoleColor(Color.WHITE);
        //V??ng tr??n m??? gi???a bi???u ?????

        pc_revenue.setTransparentCircleColor(Color.WHITE);
        pc_revenue.setTransparentCircleAlpha(110);

        pc_revenue.setHoleRadius(40f);//k??ch th?????c h??nh tr??n
        pc_revenue.setTransparentCircleRadius(61f); // k??ch th?????c h??nh tr??n m???

        pc_revenue.setRotationAngle(0);// g??c nghi???ng c???a bi???u ?????

        pc_revenue.setRotationEnabled(false);// V?? hi???u h??a xoay
        pc_revenue.setHighlightPerTapEnabled(true);// ?????t ph??ng to khi ch???n
        pc_revenue.animateXY(1000,2000);// Animation hi???n th???

        pc_revenue.setDrawEntryLabels(false);// V?? hi???u h??a m?? t??? trong bi???u ?????

        //V?? hi???u h??a ph???n ch?? th??ch
        Legend lg = pc_revenue.getLegend();
        lg.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        lg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        lg.setOrientation(Legend.LegendOrientation.VERTICAL);
        lg.setDrawInside(false);
        lg.setEnabled(false);

        //X??a m???ng d??? li???u
        for (int i = 0; i < 6; i++) {
            revenue_value[i] = 0;
        }

        //L???y th??ng tin ng?????i d??ng ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        //L???y s??? giao d???ch
        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            //L???y id v?? gi?? tr??? c???a giao d???ch
            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            //L???y th???i gian c??a giao d???ch
            String transactionDate = sharedPreferencesTransaction.getString(String.format("%s_%d", SharedPrefConstant.TRANSACTION_DATE, i), null);
            Date date =new SimpleDateFormat("dd/MM/yyyy").parse(transactionDate);
            if (transactionCategoryId >= 20) {
                if (date.getMonth() == monthOfYear && date.getYear() == year) { //Ki???m gia th???i gian giao d???ch r l??u v??o m???ng
                    setRevenueItemValueColor(transactionCategoryId, transactionMoney);
                }
            }
        }

        //T???o list d??? li???u v?? m??u cho chart
        ArrayList<PieEntry> revenue_entries = new ArrayList<>();
        revenue_entries.clear(); //x??a d??? li???u c??
        ArrayList<Integer> colors = new ArrayList<>();
        colors.clear();//x??a d??? li???u c??
        for (int i = 0; i < 6; i++) {
            if (revenue_value[i] != 0) {
                //Th??m th??ng tin v??o Arraylisst
                revenue_entries.add(new PieEntry(revenue_value[i],revenue_title[i]));
                colors.add(revenue_color[i]);
            }
        }
        //T???o d??? li???u cho pechart
        PieDataSet dataSet = new PieDataSet(revenue_entries, "");
        dataSet.setSliceSpace(0f);//Kho???ng c??ch gi???a t???ng mi???ng
        dataSet.setSelectionShift(5f);// K??ch c??? t??ng khi ??c ch???n
        dataSet.setValueTextSize(15f); // K??ch c??? ch??? gi?? tr???
        dataSet.setColors(colors);// M??u cho mi???ng
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);//??ua ph???n m?? t??? gi?? tr??? ra ngo??i

        PieData revenue_pie_data = new PieData(dataSet);//l??u gi??? li???u

        pc_revenue.setData(revenue_pie_data);// truy???n d??? li???u v??o chart
        pc_revenue.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                PieEntry pe = (PieEntry) e;
                //Hi???n th??ng tin khi nh???n v??o t???ng mi???ng qua toast
                Toast.makeText(StatisticActivity.this, "Nh??m "
                        + pe.getLabel()
                        + " c?? t???ng "
                        + e.getY()
                        + "??", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected()
            {

            }
        });
        //Ki???m tra n???u d??? li???u tr???ng th?? th??ng b??o
        if(revenue_entries.isEmpty()) {
            pc_revenue.setCenterText("D??? li???u tr???ng");
        } else {
            pc_revenue.setCenterText("");
        }
    }



    public void setExpenditurePieChart () {
        pc_expenditure.setUsePercentValues(true);
        pc_expenditure.getDescription().setEnabled(false);
        pc_expenditure.setExtraOffsets(5, 10, 5, 5);

        pc_expenditure.setDragDecelerationFrictionCoef(0.95f);

        pc_expenditure.setExtraOffsets(20.f, 0.f, 20.f, 0.f);
        //V??ng tr??n gi???a bi???u ?????

        pc_expenditure.setDrawHoleEnabled(true);
        pc_expenditure.setHoleColor(Color.WHITE);
        //V??ng tr??n m??? gi???a bi???u ?????

        pc_expenditure.setTransparentCircleColor(Color.WHITE);
        pc_expenditure.setTransparentCircleAlpha(110);

        pc_expenditure.setHoleRadius(40f);//k??ch th?????c h??nh tr??n
        pc_expenditure.setTransparentCircleRadius(61f);// k??ch th?????c h??nh tr??n m???

        pc_expenditure.setRotationAngle(0);// g??c nghi???ng c???a bi???u ?????

        pc_expenditure.setRotationEnabled(false);// V?? hi???u h??a xoay
        pc_expenditure.setHighlightPerTapEnabled(true);// ?????t ph??ng to khi ch???n

        pc_expenditure.setDrawEntryLabels(false);// V?? hi???u h??a m?? t??? trong bi???u ?????
        pc_expenditure.animateXY(1000,2000);// Animation hi???n th???
        //V?? hi???u h??a ph???n ch?? th??ch

        Legend lg = pc_expenditure.getLegend();
        lg.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        lg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        lg.setOrientation(Legend.LegendOrientation.VERTICAL);
        lg.setDrawInside(false);
        lg.setEnabled(false);

        //L???y th??ng tin ng?????i d??ng ????ng nh???p

        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        //L???y s??? giao d???ch

        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            //L???y id v?? gi?? tr??? c???a giao d???ch

            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            if (transactionCategoryId < 20 || transactionCategoryId == 26 || transactionCategoryId == 27) {
                //L??u th??ng tin giao d???ch v??o m???ng d???a theo id
                setRevenueItemValueColor(transactionCategoryId, transactionMoney);
            }
        }
        //T???o list d??? li???u v?? m??u cho chart

        ArrayList<PieEntry> expenditure_entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            if (expenditure_value[i] != 0) {
                //Th??m th??ng tin v??o Arraylisst
                expenditure_entries.add(new PieEntry(expenditure_value[i],expenditure_title[i]));
                colors.add(expenditure_color[i]);
            }
        }
        //T???o d??? li???u cho piechart

        PieDataSet dataSet = new PieDataSet(expenditure_entries, "Election Results");
        dataSet.setSliceSpace(0f);//Kho???ng c??ch gi???a t???ng mi???ng
        dataSet.setSelectionShift(5f);// K??ch c??? t??ng khi ??c ch???n
        dataSet.setValueTextSize(15f);// K??ch c??? ch??? gi?? tr???
        dataSet.setColors(colors);// M??u cho mi???ng
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);//??ua ph???n m?? t??? gi?? tr??? ra ngo??i

        PieData revenue_pie_data = new PieData(dataSet);//l??u gi??? li???u

        pc_expenditure.setData(revenue_pie_data);// truy???n d??? li???u v??o chart

        pc_expenditure.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                //Hi???n th??ng tin khi nh???n v??o t???ng mi???ng qua toast
                Toast.makeText(StatisticActivity.this, "Nh??m "
                        + pe.getLabel()
                        + " c?? t???ng "
                        + e.getY()
                        + "??", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        //Ki???m tra n???u d??? li???u tr???ng th?? th??ng b??o

        if(expenditure_entries.isEmpty()) {
            pc_expenditure.setCenterText("D??? li???u tr???ng");
        } else {
            pc_expenditure.setCenterText("");
        }
    }

    public void setExpenditurePieChartByDate (int year, int monthOfYear) throws ParseException {
        pc_expenditure.setUsePercentValues(true);
        pc_expenditure.getDescription().setEnabled(false);
        pc_expenditure.setExtraOffsets(5, 10, 5, 5);

        pc_expenditure.setDragDecelerationFrictionCoef(0.95f);

        pc_expenditure.setExtraOffsets(20.f, 0.f, 20.f, 0.f);
        //V??ng tr??n gi???a bi???u ?????

        pc_expenditure.setDrawHoleEnabled(true);
        pc_expenditure.setHoleColor(Color.WHITE);
        //V??ng tr??n m??? gi???a bi???u ?????

        pc_expenditure.setTransparentCircleColor(Color.WHITE);
        pc_expenditure.setTransparentCircleAlpha(110);

        pc_expenditure.setHoleRadius(40f);//k??ch th?????c h??nh tr??n
        pc_expenditure.setTransparentCircleRadius(61f);// k??ch th?????c h??nh tr??n m???

        pc_expenditure.setRotationAngle(0);// g??c nghi???ng c???a bi???u ?????

        pc_expenditure.setRotationEnabled(false);// V?? hi???u h??a xoay
        pc_expenditure.setHighlightPerTapEnabled(true);// ?????t ph??ng to khi ch???n

        pc_expenditure.setDrawEntryLabels(false);// V?? hi???u h??a m?? t??? trong bi???u ?????
        pc_expenditure.animateXY(1000,2000);// Animation hi???n th???

        //V?? hi???u h??a ph???n ch?? th??ch
        Legend lg = pc_expenditure.getLegend();
        lg.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        lg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        lg.setOrientation(Legend.LegendOrientation.VERTICAL);
        lg.setDrawInside(false);
        lg.setEnabled(false);

        //X??a m???ng d??? li???u
        for (int i = 0; i < 19; i++) {
            expenditure_value[i] = 0;
        }
        //L???y th??ng tin ng?????i d??ng ????ng nh???p
        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
        String username = sharedPreferencesSigningIn.getString(SharedPrefConstant.SIGNING_IN_USERNAME, "");
        SharedPreferences sharedPreferencesTransaction = getSharedPreferences(username, MODE_PRIVATE);
        //L???y s??? giao d???ch
        int totalTransactions = sharedPreferencesTransaction.getInt(SharedPrefConstant.TRANSACTION_TOTAL, 0);
        for(int i = 1; i <= totalTransactions; ++i){
            //L???y th???i gian c??a giao d???ch
            String transactionDate = sharedPreferencesTransaction.getString(String.format("%s_%d", SharedPrefConstant.TRANSACTION_DATE, i), null);
            Date date =new SimpleDateFormat("dd/MM/yyyy").parse(transactionDate);
            //L???y id v?? gi?? tr??? c???a giao d???ch
            int transactionCategoryId = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_CATEGORY_ID, i), 0);
            int transactionMoney = sharedPreferencesTransaction.getInt(String.format("%s_%d", SharedPrefConstant.TRANSACTION_MONEY, i), 0);
            if (transactionCategoryId < 20 || transactionCategoryId == 26 || transactionCategoryId == 27) {//Ki???m gia th???i gian giao d???ch r l??u v??o m???ng
                if (date.getMonth() == monthOfYear && date.getYear() == year) {
                    setRevenueItemValueColor(transactionCategoryId, transactionMoney);
                }
            }
        }
        //T???o list d??? li???u v?? m??u cho chart
        ArrayList<PieEntry> expenditure_entries = new ArrayList<>();
        expenditure_entries.clear();//x??a d??? li???u c??
        ArrayList<Integer> colors = new ArrayList<>();
        colors.clear();//x??a d??? li???u c??
        for (int i = 0; i < 19; i++) {
            if (expenditure_value[i] != 0) {
                //Th??m th??ng tin v??o Arraylisst
                expenditure_entries.add(new PieEntry(expenditure_value[i],expenditure_title[i]));
                colors.add(expenditure_color[i]);
            }
        }
        //T???o d??? li???u cho pechart

        PieDataSet dataSet = new PieDataSet(expenditure_entries, "Election Results");
        dataSet.setSliceSpace(0f);//Kho???ng c??ch gi???a t???ng mi???ng
        dataSet.setSelectionShift(5f);// K??ch c??? t??ng khi ??c ch???n
        dataSet.setValueTextSize(15f);// K??ch c??? ch??? gi?? tr???
        dataSet.setColors(colors);// M??u cho mi???ng
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);//??ua ph???n m?? t??? gi?? tr??? ra ngo??i

        PieData revenue_pie_data = new PieData(dataSet);//l??u gi??? li???u

        pc_expenditure.setData(revenue_pie_data);// truy???n d??? li???u v??o chart

        pc_expenditure.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                //Hi???n th??ng tin khi nh???n v??o t???ng mi???ng qua toast

                Toast.makeText(StatisticActivity.this, "Nh??m "
                        + pe.getLabel()
                        + " c?? t???ng "
                        + e.getY()
                        + "??", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        //Ki???m tra n???u d??? li???u tr???ng th?? th??ng b??o
        if(expenditure_entries.isEmpty()) {
            pc_expenditure.setCenterText("D??? li???u tr???ng");
        } else {
            pc_expenditure.setCenterText("");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        getViews();
        //Kh??ng ?????i ch??? 2 d??ng d?????i cho nhau
        //H??m setSelected item ho???t ?????ng nh?? ??ang ???n v??o menu ???? => trigger event onNavigationItemSelected => bug
        //=> set item ???????c ch???n tr?????c khi set event
        SharedMethods.setNavigationMenu(bnv_menu, R.id.item_statistic);
        setEventListener();
        displayUserInformation();
        getWalletMoney();
        getRevenueExpenditure();
        setDateLitener();
        setRevenuePieChart();
        setExpenditurePieChart();
    }

}
