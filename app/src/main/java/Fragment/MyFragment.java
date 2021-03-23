package Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.notification.R;

public class MyFragment extends Fragment {
    private int num;
            public MyFragment(int content) {
        this.num = content;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.num==1) {
            View view = inflater.inflate(R.layout.frament_home, container, false);
            return view;
        }
        else{
            View view = inflater.inflate(R.layout.locate, container, false);
            return view;
        }
    }
}
