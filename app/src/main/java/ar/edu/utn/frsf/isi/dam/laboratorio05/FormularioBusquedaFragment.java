package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class FormularioBusquedaFragment extends Fragment {

    private ReclamoArrayAdapter adapter;
    private ReclamoDao reclamoDao;

    private Spinner tipoReclamo;
    private Button btnBuscar;
    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    private NuevoReclamoFragment.OnNuevoLugarListener listener;

    public FormularioBusquedaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        View v = inflater.inflate(R.layout.fragment_formulario, container, false);

        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);
        btnBuscar= (Button) v.findViewById(R.id.btnBuscar);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment= new MapaFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("tipo_mapa",5);
                bundle.putString("tipo_reclamo",tipoReclamo.getSelectedItem().toString());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contenido, fragment)
                        .commit();
            }
        });

        return v;
    }
}
