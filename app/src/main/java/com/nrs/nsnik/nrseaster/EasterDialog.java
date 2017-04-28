package com.nrs.nsnik.nrseaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class EasterDialog extends DialogFragment {


    private static final int[] m1 = {116, 104, 101, 71, 111, 111, 100, 71, 117, 121};
    private static final int[] m2 = {78, 101, 119, 97, 110, 116};
    private static final int[] m3 = {78, 111, 111, 98, 105, 101, 32, 78, 111, 111, 98, 101, 114, 115, 111, 110};
    private static String mNrsImageUrl = null;
    private static String mMyImageUrl = null;
    private static String mReyImageUrl = null;
    private static String mMenuImageUrl = null;
    private static int mPassword;
    ImageView mNrsImage, mMyImage;
    TextView mNrsText, mMyText, mLoadingText;
    EditText mPasswordText;
    ProgressBar mNrsImageProgress, mMyImageProgress;
    LinearLayout mImageContainer, mPasswordContainer;
    Button mSubmit;
    private int mCounter = 1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_easter, container, false);
        initialize(v);
        mSubmit.setVisibility(View.GONE);
        mPasswordText.setVisibility(View.GONE);
        String host = getResources().getString(R.string.urlServer);
        String queryFilename = getResources().getString(R.string.urlMineQueryAll);
        String url = host + queryFilename;
        try {
            new GetValuesAsync().execute(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mSubmit.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        }
        return v;
    }


    private void initialize(View v) {
        mNrsImage = (ImageView) v.findViewById(R.id.myNrsImage);
        mMyImage = (ImageView) v.findViewById(R.id.myPImage);
        mNrsText = (TextView) v.findViewById(R.id.myNrsText);
        mMyText = (TextView) v.findViewById(R.id.myPTEXT);
        mNrsImageProgress = (ProgressBar) v.findViewById(R.id.myNrsImageProgress);
        mMyImageProgress = (ProgressBar) v.findViewById(R.id.myPImageProgress);
        mImageContainer = (LinearLayout) v.findViewById(R.id.myImageContainer);
        mPasswordContainer = (LinearLayout) v.findViewById(R.id.myPasswordContainer);
        mSubmit = (Button) v.findViewById(R.id.myPasswordGo);
        mPasswordText = (EditText) v.findViewById(R.id.myPasswordText);
        mLoadingText = (TextView) v.findViewById(R.id.myPasswordLoadingText);
    }

    private InputStream getValues(URL url) {
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return inputStream;
    }

    private String getJson(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        StringBuilder stringBuilder = new StringBuilder();
        while (line != null) {
            stringBuilder.append(line);
            line = bufferedReader.readLine();
        }
        return stringBuilder.toString();
    }

    private void setValues(String jsonArray) throws JSONException {
        JSONArray array = new JSONArray(jsonArray);
        if (array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("id");
                String value = object.getString("value");
                set(id, value.replaceAll("\\/", "/"));
                if (i == 2) {
                    setEnabled();
                    clickListener();
                }
            }
        }
    }

    private void setEnabled() {
        if (mLoadingText != null) {
            mLoadingText.setVisibility(View.GONE);
        }
        if (mSubmit != null) {
            mSubmit.setVisibility(View.VISIBLE);
        }
        if (mPasswordText != null) {
            mPasswordText.setVisibility(View.VISIBLE);
        }
    }

    private void set(int id, String v) {
        switch (id) {
            case 1:
                mNrsImageUrl = v;
                break;
            case 2:
                mMyImageUrl = v;
                break;
            case 3:
                mPassword = Integer.parseInt(v);
                break;
            case 4:
                mReyImageUrl = v;
                break;
            case 5:
                mMenuImageUrl = v;
                break;
        }
    }

    private void clickListener() {
        if (mSubmit != null) {
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkConnection()) {
                        if (!mPasswordText.getText().toString().isEmpty() || mPasswordText.getText().toString().length() != 0) {
                            int password = Integer.parseInt(mPasswordText.getText().toString());
                            if (password == mPassword) {
                                mPasswordContainer.setVisibility(View.GONE);
                                mImageContainer.setVisibility(View.VISIBLE);
                                setImages();
                            } else {
                                mPasswordText.setText("");
                                mPasswordText.setFocusable(true);
                                mPasswordText.setError("Wrong password");
                            }
                        } else {
                            mPasswordText.setFocusable(true);
                            mPasswordText.setError("Enter the password to see the magic");
                        }
                    } else {
                        Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void setImages() {
        mNrsImageProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.MULTIPLY);
        mMyImageProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.MULTIPLY);
        mNrsImage.setBackgroundColor(Color.parseColor("#000000"));
        mMyImage.setBackgroundColor(Color.parseColor("#000000"));
        GetImagesAsync getImagesAsync = new GetImagesAsync(mNrsImage, mNrsImageProgress);
        try {
            getImagesAsync.execute(new URL(mNrsImageUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        GetImagesAsync getProfileImagesAsync = new GetImagesAsync(mMyImage, mMyImageProgress, mkn(m1));
        try {
            getProfileImagesAsync.execute(new URL(mMyImageUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        setClick();
    }

    private Bitmap getImages(URL u) {
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream;
        Bitmap b = null;
        try {
            httpURLConnection = (HttpURLConnection) u.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            b = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return b;
    }

    private void setClick() {
        if (mMyImage != null) {
            mMyImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCounter == 0) {
                        mCounter++;
                        GetImagesAsync getProfileImagesAsync = new GetImagesAsync(mMyImage, mMyImageProgress, mkn(m1));
                        try {
                            getProfileImagesAsync.execute(new URL(mMyImageUrl));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    } else if (mCounter == 1) {
                        mCounter++;
                        GetImagesAsync getProfileImagesAsync = new GetImagesAsync(mMyImage, mMyImageProgress, mkn(m2));
                        try {
                            getProfileImagesAsync.execute(new URL(mReyImageUrl));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    } else if (mCounter == 2) {
                        mCounter = 0;
                        GetImagesAsync getProfileImagesAsync = new GetImagesAsync(mMyImage, mMyImageProgress, mkn(m3));
                        try {
                            getProfileImagesAsync.execute(new URL(mMenuImageUrl));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private String mkn(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int anArr : arr) {
            sb.append(Character.toString((char) anArr));
        }
        return sb.toString();
    }

    private class GetValuesAsync extends AsyncTask<URL, Void, InputStream> {

        @Override
        protected InputStream doInBackground(URL... params) {
            return getValues(params[0]);
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            try {
                setValues(getJson(inputStream));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetImagesAsync extends AsyncTask<URL, Void, Bitmap> {

        ImageView mImageView;
        ProgressBar mProgressBar;
        String mText;
        URL mUrl = null;

        GetImagesAsync(ImageView imageView, ProgressBar progressBar) {
            mImageView = imageView;
            mProgressBar = progressBar;
        }

        GetImagesAsync(ImageView imageView, ProgressBar progressBar, String text) {
            mImageView = imageView;
            mProgressBar = progressBar;
            mText = text;
        }

        @Override
        protected Bitmap doInBackground(URL... params) {
            mUrl = params[0];
            return getImages(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mProgressBar.setVisibility(View.GONE);
            if (mText != null) {
                mMyText.setText(mText);
            }
            super.onPostExecute(bitmap);
        }
    }

}
