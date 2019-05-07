package cat.bcn.ratememaybe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.util.Locale;
import java.util.Map;

public class RateMeMaybeFragment extends DialogFragment implements
        DialogInterface.OnClickListener, OnCancelListener {

    protected static final int UNDEFINED = -1;

    private RMMFragInterface mInterface;
    private int customIcon;
    private int buttonsTextColor;
    private int titleColor;
    private int messageColor;
    private int backgroundColor;
    private Map<String, String> messages;
    private String language;

    public void setData(int customIcon, RMMFragInterface myInterface, int buttonsTextColor, int titleColor, int messageColor,
                        int backgroundColor, Map<String, String> messages, String language) {
        this.customIcon = customIcon;
        this.mInterface = myInterface;
        this.buttonsTextColor = buttonsTextColor;
        this.titleColor = titleColor;
        this.messageColor = messageColor;
        this.backgroundColor = backgroundColor;
        this.messages = messages;
        this.language = language;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fragment including variables will survive orientation changes
        this.setRetainInstance(true);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RateMeMaybeDialog);

        if (customIcon != 0) {
            builder.setIcon(customIcon);
        }

        try {
            if (customIcon != 0) {
                builder.setIcon(customIcon);
            } else {
                builder.setIcon(getContext().getPackageManager().getApplicationIcon(getContext().getPackageName()));
            }
        } catch (Exception e) {
            builder.setIcon(android.R.drawable.sym_def_app_icon);
        }

        String appName = getApplicationName();
        builder.setTitle(getString(R.string.dialog_rating_title, appName));
        builder.setPositiveButton(R.string.dialog_rating_positive, this);
        builder.setNeutralButton(R.string.dialog_rating_neutral, this);
        builder.setNegativeButton(R.string.dialog_rating_negative, this);
        builder.setOnCancelListener(this);
        String message = this.messages.get(language);
        builder.setMessage(TextUtils.isEmpty(message) ? getString(R.string.dialog_rating_message, appName) :
                message);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(onShowListener);
        return alertDialog;
    }


    private String getApplicationName() {
        ApplicationInfo applicationInfo = getContext().getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getContext().getString(stringId);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        mInterface._handleCancel();
    }


    @Override
    public void onClick(DialogInterface dialog, int choice) {
        switch (choice) {
            case DialogInterface.BUTTON_POSITIVE:
                mInterface._handlePositiveChoice();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mInterface._handleNeutralChoice();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mInterface._handleNegativeChoice();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        Dialog dialog = getDialog();

        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialog) {
            customizeDialogColors();
        }
    };

    private void customizeDialogColors() {
        if (buttonsTextColor != UNDEFINED) {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonsTextColor);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonsTextColor);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(buttonsTextColor);
        }

        if (titleColor != UNDEFINED) {
            int tvTitleId = getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            TextView tvTitle = (TextView) getDialog().findViewById(tvTitleId);
            if (tvTitle != null) {
                tvTitle.setTextColor(titleColor);
            }
            int titleDividerId = getResources().getIdentifier("android:id/titleDivider", null, null);
            View titleDivider = getDialog().findViewById(titleDividerId);
            if (titleDivider != null) {
                titleDivider.setBackgroundColor(titleColor);
            }
        }

        if (messageColor != UNDEFINED) {
            int tvMessageId = getContext().getResources().getIdentifier("android:id/message", null, null);
            TextView tvMessage = (TextView) getDialog().findViewById(tvMessageId);
            if (tvMessage != null) {
                tvMessage.setTextColor(messageColor);
            }
        }

        if (backgroundColor != UNDEFINED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getDialog().getWindow();
                if (window != null) {
                    window.getDecorView().setBackgroundColor(backgroundColor);
                }
            } else {
                int parentPanelId = getContext().getResources().getIdentifier("android:id/parentPanel", null, null);
                ViewGroup parentPanel = (ViewGroup) getDialog().findViewById(parentPanelId);
                if (parentPanel != null) {
                    for (int index = 0; index < parentPanel.getChildCount(); index++) {
                        parentPanel.getChildAt(index).setBackgroundColor(backgroundColor);
                    }
                }
            }
        }
    }

    public interface RMMFragInterface {
        void _handlePositiveChoice();

        void _handleNeutralChoice();

        void _handleNegativeChoice();

        void _handleCancel();
    }

}