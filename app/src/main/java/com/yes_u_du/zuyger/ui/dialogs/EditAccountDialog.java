package com.yes_u_du.zuyger.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.FirebaseDatabase;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.utils.EditAccountHelper;

import java.util.HashMap;


public class EditAccountDialog extends DialogFragment implements EditAccountHelper {
    private static final int CALENDAR_REQUEST = 11;

    //private EditText nameText;
    //private EditText surnameText;
    private Spinner countrySpinner;
    private EditText cityText;
    private boolean isRefusedPhotosFromAll;
    private Spinner regionSpinner;
    //private EditText ageText;
    private EditText aboutText;
    // private Spinner sexSpinner;
    //private Button editAge;
    //private Date dateOfBirth;

    private ArrayAdapter<CharSequence> countryAdapter;
    private ArrayAdapter<CharSequence> regionAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_account_dialog, null);
        //nameText = view.findViewById(R.id.name);
        //nameText.setText(User.getCurrentUser().getName());
        // surnameText = view.findViewById(R.id.surname);
        //surnameText.setText(User.getCurrentUser().getSurname());
        cityText = view.findViewById(R.id.city);
        //todo cityText.setText(UserModel.getCurrentUser().getCity());
        aboutText = view.findViewById(R.id.about_edit_text);
        aboutText.setText(UserModel.getCurrentUser().getAbout());
        //sexSpinner = view.findViewById(R.id.spinner_sex_edit);
        countrySpinner = view.findViewById(R.id.country);
        regionSpinner = view.findViewById(R.id.region);
        regionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.region_filter_rus, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        countryAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.country_filter, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: {
                        regionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.region_filter_rus, android.R.layout.simple_spinner_item);
                        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpinner.setAdapter(regionAdapter);
                        /*todoregionSpinner.setSelection(Integer.parseInt(UserModel.getCurrentUser().getRegion()));*/
                    }
                    break;
                    case 2: {
                        regionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.region_filter_armenia, android.R.layout.simple_spinner_item);
                        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpinner.setAdapter(regionAdapter);
                        /*todo
                        if (Integer.parseInt(UserModel.getCurrentUser().getRegion()) < 10)
                            regionSpinner.setSelection(Integer.parseInt(UserModel.getCurrentUser().getRegion()));*/
                    }
                    break;
                    case 4: {
                        regionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.region_filter_usa, android.R.layout.simple_spinner_item);
                        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpinner.setAdapter(regionAdapter);
                        /*
                        if (Integer.parseInt(UserModel.getCurrentUser().getRegion()) < 50)
                            regionSpinner.setSelection(Integer.parseInt(UserModel.getCurrentUser().getRegion()));
                            todo
                         */
                    }
                    break;
                    default: {
                        regionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.no_region_filter, android.R.layout.simple_spinner_item);
                        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpinner.setAdapter(regionAdapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        SwitchMaterial switchMaterial = view.findViewById(R.id.permit_getting_photos);
        switchMaterial.setChecked(UserModel.getCurrentUser().isRefusePhotosFromAll());
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRefusedPhotosFromAll = isChecked;
        });
        //todo countrySpinner.setSelection(Integer.parseInt(UserModel.getCurrentUser().getCountry()));

        // if (User.getCurrentUser().getSex().equals(getResources().getStringArray(R.array.sex_for_spinner)[0]))
        //    sexSpinner.setSelection(0);
        //else sexSpinner.setSelection(1);
        // ageText = view.findViewById(R.id.age);
        //ageText.setText(User.getCurrentUser().getAge());

        /*editAge = view.findViewById(R.id.edit_birth_call_calendar);
        editAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDialog dialog = CalendarDialog.newInstance(new Date(User.getCurrentUser().getDateBirthday()));
                dialog.setTargetFragment(EditAccountDialog.this,CALENDAR_REQUEST);
                dialog.show(getFragmentManager(),null);
            }
        });*/


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle(getResources().getString(R.string.dialog_title_edit))
                .setView(view)
                .setPositiveButton(R.string.ok_pos_button_text, (dialog, which) -> updateUser())
                .create();
    }

    private void updateUser() {
        final String country = toFirstUpperCase(String.valueOf(countrySpinner.getSelectedItemPosition()).trim());
        final String region = toFirstUpperCase(String.valueOf(regionSpinner.getSelectedItemPosition()).trim());
        /*Calendar calendar =Calendar.getInstance();
        calendar.setTime(dateOfBirth);
        RegisterFragment.AgeCalculation ageCalculation = new RegisterFragment.AgeCalculation();
        ageCalculation.getCurrentDate();
        ageCalculation.setDateOfBirth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        final String age = String.valueOf(ageCalculation.calculateYear());*/
        if (cityText.getText().toString().isEmpty()
                || country.equals("0") || (country.equals("1") && region.equals("0")
                || (country.equals("2") && region.equals("0")) || (country.equals("3") && region.equals("0")))) {
            Toast.makeText(getActivity(), R.string.reject_update, Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            //hashMap.put("name", nameText.getText().toString());
            //User.getCurrentUser().setName(nameText.getText().toString());
            //  hashMap.put("surname", surnameText.getText().toString());
            // User.getCurrentUser().setSurname(surnameText.getText().toString());
            hashMap.put("city", toFirstUpperCase(cityText.getText().toString().trim()));
            //todo UserModel.getCurrentUser().setCity(toFirstUpperCase(cityText.getText().toString().trim()));
            hashMap.put("country", toFirstUpperCase(String.valueOf(countrySpinner.getSelectedItemPosition())));
            //todo UserModel.getCurrentUser().setCountry(toFirstUpperCase(String.valueOf(countrySpinner.getSelectedItemPosition()).trim()));
            hashMap.put("region", toFirstUpperCase(String.valueOf(regionSpinner.getSelectedItemPosition()).trim()));
            //todo UserModel.getCurrentUser().setRefusePhotosFromAll(isRefusedPhotosFromAll);
            hashMap.put("refusePhotosFromAll", isRefusedPhotosFromAll);
            //todo UserModel.getCurrentUser().setRegion(toFirstUpperCase(String.valueOf(regionSpinner.getSelectedItemPosition())));
            //hashMap.put("sex", sexSpinner.getSelectedItem().toString());
            //User.getCurrentUser().setSex(sexSpinner.getSelectedItem().toString());
            // hashMap.put("age", ageText.getText().toString());
            //User.getCurrentUser().setAge(ageText.getText().toString());
            //hashMap.put("age", age);
            //User.getCurrentUser().setAge(age);
            //hashMap.put("dateBirthday", dateOfBirth.getTime());
            //User.getCurrentUser().setDateBirthday(dateOfBirth.getTime());
            hashMap.put("about", toFirstUpperCase(aboutText.getText().toString()));
            UserModel.getCurrentUser().setAbout(toFirstUpperCase(aboutText.getText().toString()));
            FirebaseDatabase.getInstance().getReference("users").child(UserModel.getCurrentUser().getUuid()).updateChildren(hashMap);
        }
    }

    @Nullable
    @Override
    public String toFirstUpperCase(@NonNull String str) {
        if (str.length() > 0)
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        else return "";
    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALENDAR_REQUEST && resultCode == RESULT_OK){
            dateOfBirth = (Date) data.getSerializableExtra(CalendarDialog.EXTRA_DATE);
        }
    }*/
}
