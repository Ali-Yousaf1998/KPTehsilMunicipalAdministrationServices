package com.uzair.kptehsilmunicipaladministrationservices.AppModules.UserComplaint.ComplaintsMain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uzair.kptehsilmunicipaladministrationservices.AppModules.UserComplaint.AdapterOfComplaintRecycler.SelectedImageRecyclerAdapter;
import com.uzair.kptehsilmunicipaladministrationservices.Models.AddComplaintsPresenterImplementer;
import com.uzair.kptehsilmunicipaladministrationservices.R;
import com.uzair.kptehsilmunicipaladministrationservices.Views.AddComplaintsView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class AddComplaints extends AppCompatActivity implements AddComplaintsView
{

    public static final int REQUEST_CODE = 1;
    public static final int LOCATION_REQUEST_CODE = 110;
    private Toolbar mToolbar;
    private EditText editTextTitle, editTextDescription;
    private RadioButton sanitation, infrastructure;
    private RecyclerView mRecyclerView;
    private ProgressDialog progressDialog;

    private SelectedImageRecyclerAdapter adapter;
    private List<Uri> imageUriList;
    private  double lat , lng;
    private AddComplaintsPresenterImplementer presenterImplementer;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_complaints);

        initViews();


    }

    private void initViews() {

        presenterImplementer = new AddComplaintsPresenterImplementer(this , this);

        //  app tool bar
        mToolbar = findViewById(R.id.addComplain_tool_bar);
        setSupportActionBar(mToolbar);
        setTitle("Add Complaint");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.backicon);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // edit text
        editTextTitle = findViewById(R.id.complaint_title);
        editTextDescription = findViewById(R.id.complaint_description_edit_text);

        // radio buttons
        sanitation = findViewById(R.id.rd_btn_sanitation);
        infrastructure = findViewById(R.id.rd_btn_infrastructure);

        // progress dialog
        progressDialog = new ProgressDialog(this , R.style.MyAlertDialogStyle);

        // recycler view
        imageUriList = new ArrayList<>();


        //firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Complaints");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Photo");
        mAuth = FirebaseAuth.getInstance();


    }

    // select image button click
    public void uploadComplaintImageBtn(View view) {

        imageUriList.clear();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);

    }

    // submit complaint button click
    public void submitComplaintBtn(View view) {

        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();


                if(sanitation.isChecked())
                {
                    presenterImplementer.storeComplaintDataToFirebase(mDatabaseReference ,mAuth , mStorageReference
                    ,title , description , "Sanitation" , imageUriList , lat , lng);
                }
                else if(infrastructure.isChecked())
                {
                    presenterImplementer.storeComplaintDataToFirebase(mDatabaseReference ,mAuth , mStorageReference
                            ,title , description , "Infrastructure" , imageUriList ,lat , lng);
                }
                else {
                Toast.makeText(this, "Please select your department", Toast.LENGTH_SHORT).show();
            }


    }

    // add location click
    public void addLocationClick(View view)
    {
      Intent intent = new Intent(AddComplaints.this , MapActivity.class);
      startActivityForResult(intent , LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //check condition for Gallery image picker request code
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            // get multiple images from gallery
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    //add images to arraylist
                    imageUriList.add(clipData.getItemAt(i).getUri());

                    setRecyclerView(imageUriList);
                }
            }
            else
            {
                Uri uri = data.getData();
                imageUriList.add(uri);
                setRecyclerView(imageUriList);
            }
        }


        // Check location result with an OK result
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

                // Get String data from Intent
                lat = data.getDoubleExtra("lat" , 0.0);
                lng = data.getDoubleExtra("lng" , 0.0);

                Log.d("ResultLocation", "onCreate: "+lat+"\n"+lng);
        }


    }

    // set select image in recycler view
    private void setRecyclerView(List<Uri> uris)
    {
        mRecyclerView = findViewById(R.id.selectImagesRecyclerList);
        adapter = new SelectedImageRecyclerAdapter(uris , this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this , 3));
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void showProgressBar() {
        progressDialog.setMessage("Uploading please wait...");
        progressDialog.setTitle("Complaint Data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    public void hideProgressBar() {
        progressDialog.dismiss();
    }

    @Override
    public void closeActivity()
    {
        startActivity(new Intent(getApplicationContext(), Complaints.class));
        this.finish();
    }

    @Override
    public void showMessage(String message, String type) {
        if (type.equals("info")) {
         Toasty.info(this,message,Toasty.LENGTH_SHORT).show();
        } else if (type.equals("error")) {
            Toasty.error(this,message,Toasty.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void clearAllFields() {
        editTextTitle.setText("");
        editTextDescription.setText("");
    }

}




