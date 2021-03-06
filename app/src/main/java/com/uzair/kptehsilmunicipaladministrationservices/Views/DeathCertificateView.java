package com.uzair.kptehsilmunicipaladministrationservices.Views;

import android.net.Uri;

import java.util.List;

public interface DeathCertificateView {
    void onShowProgressDialog();

    void onDismissProgressDialog();

    void showMessage(String message, String type);

    void setSpinnerAdapter();

    void chooseGallery();

    void displayImagePreview(List<Uri> mFileUri);

    void clearAllFileds();

    void closeActivity();
}
