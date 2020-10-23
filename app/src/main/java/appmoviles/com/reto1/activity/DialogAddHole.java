package appmoviles.com.reto1.activity;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import appmoviles.com.reto1.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DialogAddHole#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogAddHole extends DialogFragment implements View.OnClickListener {

    private Button butOk;
    private Button butCancelar;
    private TextView address;
    private TextView latLng;
    private Listener listener;

    private String ad;
    private String latlng;

    public DialogAddHole() {
        // Required empty public constructor
    }

    public static DialogAddHole newInstance() {
        DialogAddHole fragment = new DialogAddHole();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dialog_add_hole, container, false);
        butOk = root.findViewById(R.id.butOk);
        butCancelar = root.findViewById(R.id.butCancelar);
        latLng = root.findViewById(R.id.lntLng);
        address = root.findViewById(R.id.ads);
        latLng.setText(latlng);
        address.setText(ad);

        butOk.setOnClickListener(this);
        butCancelar.setOnClickListener(this);

        return root;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.butOk:
                listener.onOk();
                break;
            case R.id.butCancelar:
                listener.onCancelar();
                break;
        }
    }

    public TextView getAddress() {
        return address;
    }

    public void setAddress(TextView address) {
        this.address = address;
    }

    public TextView getLatLng() {
        return latLng;
    }

    public void setLatLng(TextView latLng) {
        this.latLng = latLng;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public interface Listener{
        void onOk();
        void onCancelar();
    }
}