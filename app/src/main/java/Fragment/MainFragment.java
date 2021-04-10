package Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.notification.Locate;
import com.example.notification.MainActivity;
import com.example.notification.R;
import com.google.android.gms.maps.SupportMapFragment;

public class MainFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;

    //Fragment Object
    private MyFragment main;
    private Locate locate;
    //private MainFragment fragment_main;
    private FragmentManager fManager;
    SupportMapFragment mapFragment ;
    public MainFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_main, container, false);
        ((MainActivity)getActivity()).setActionBarTitle("Home");
        // Inflate the layout for this fragment
        fManager = getFragmentManager();
        rg_tab_bar = (RadioGroup) view.findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        rb_main = (RadioButton) view.findViewById(R.id.rb_main);
        rb_main.setChecked(true);
        return view;

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (checkedId) {
            case R.id.rb_main:
                if (main == null) {
                    main = new MyFragment();
                    fTransaction.add(R.id.ly_content, main);
                } else {
                    fTransaction.show(main);
                }
                break;
            case R.id.rb_locate:
                if (locate == null) {
                    locate=new Locate();
                    fTransaction.add(R.id.ly_content,locate);
                } else {
                    fTransaction.show(locate);
                }
                break;
        }
        fTransaction.commit();

    }

    private void hideAllFragment(FragmentTransaction fTransaction) {
        if (main != null) fTransaction.hide(main);
        if (locate != null) fTransaction.hide(locate);
    }
}
