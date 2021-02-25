package com.example.basicviewer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.basicviewer.conn.RetrofitInterface;
import com.example.basicviewer.database.DataAllSensor;
import com.example.login_nodejs.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Collection;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static com.example.basicviewer.conn.RetrofitInterface.BASE_URL;
//import  com.example.basicviewer.conn.LoggedInUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LastDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LastDataFragment extends Fragment {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;

    DataAllSensor dataAllSensor;

    GraphView graphPPG,graphEKG, graphEMG, graphAcce, graphSuhu;
    Integer[] dataPPG, dataEKG, dataEMG, dataAcceX, dataAcceY, dataAcceZ, dataSuhu;
    private LineGraphSeries<DataPoint> seriesPPG, seriesEKG, seriesEMG, seriesAcceX, seriesAcceY, seriesAcceZ, seriesSuhu;

    String nameUser,idUser;
    int i;
    private static final String ARG_SECTION_NUMBER = "section_number";
    View root;
    public LastDataFragment() {
        // Required empty public constructor
    }

    public static LastDataFragment newInstance(String param1, String param2) {
        LastDataFragment fragment = new LastDataFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_last, container, false);
        //Grafik Set up
        //PPG
        graphPPG = (GraphView) root.findViewById(R.id.graphLivePPG);
        //EKG
        graphEKG = (GraphView) root.findViewById(R.id.graphLiveEKG);
        //EMG
        graphEMG = (GraphView) root.findViewById(R.id.graphLiveEMG);
        //Acce
        graphAcce = (GraphView) root.findViewById(R.id.graphLiveAcce);
        //Suhu
        graphSuhu = (GraphView) root.findViewById(R.id.graphLiveSuhu);

        setGraph(graphPPG);
        setGraph(graphEKG);
        setGraph(graphEMG);
        setGraph(graphAcce);
        setGraph(graphAcce);
        setGraph(graphAcce);
        setGraph(graphSuhu);
        graphSuhu.getViewport().setMinY(20);

        setLoggedInUser();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        resetGraph();
        handleGetRekonstruksiDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        Button fab = root.findViewById(R.id.refreshButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGraph();
                handleGetRekonstruksiDialog();
            }
        });
    }

    private DataAllSensor handleGetRekonstruksiDialog() {

        Call<Collection<DataAllSensor>> call = retrofitInterface.getDataAll(idUser);

        call.enqueue(new Callback<Collection<DataAllSensor>>() {
            @Override
            public void onResponse(Call<Collection<DataAllSensor>> call, Response<Collection<DataAllSensor>> response) {
                response.code();
                Collection<DataAllSensor> result = response.body();

                try {
                    DataAllSensor[] dataAllSensorAll = result.toArray(new DataAllSensor[result.size()]);
                    dataAllSensor = dataAllSensorAll[0];
                    plotData();
                }catch (Exception e){
                    Toast.makeText(getActivity(),"No Data",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Collection<DataAllSensor>> call, Throwable t) {
                //Toast.makeText(getActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),"There is a problem.\nPlease check your internet connection or try again later",Toast.LENGTH_LONG).show();

            }
        });
        return dataAllSensor;
    }

    //
    public void plotData(){
        //PPG
        dataPPG = dataAllSensor.getDataPPG().toArray(new Integer[dataAllSensor.getDataPPG().size()]);
        //EKG
        dataEKG = dataAllSensor.getDataEKG().toArray(new Integer[dataAllSensor.getDataEKG().size()]);
        //EMG
        dataEMG = dataAllSensor.getDataEMG().toArray(new Integer[dataAllSensor.getDataEMG().size()]);
        //Acce X
        dataAcceX = dataAllSensor.getDataAccelerometer_X().toArray(new Integer[dataAllSensor.getDataAccelerometer_X().size()]);
        //Acce Y
        dataAcceY = dataAllSensor.getDataAccelerometer_Y().toArray(new Integer[dataAllSensor.getDataAccelerometer_Y().size()]);
        //Acce Z
        dataAcceZ = dataAllSensor.getDataAccelerometer_Z().toArray(new Integer[dataAllSensor.getDataAccelerometer_Z().size()]);
        //Suhu
        dataSuhu = dataAllSensor.getDataSuhu().toArray(new Integer[dataAllSensor.getDataSuhu().size()]);

        drawGraph(graphPPG, dataPPG, seriesPPG, Color.BLUE);
        drawGraph(graphEKG,dataEKG, seriesEKG, Color.LTGRAY);
        drawGraph(graphEMG,dataEMG, seriesEMG, Color.GREEN);
        drawGraph(graphAcce,dataAcceX, seriesAcceX, Color.MAGENTA);
        drawGraph(graphAcce,dataAcceY, seriesAcceY, Color.CYAN);
        drawGraph(graphAcce,dataAcceZ, seriesAcceZ, Color.YELLOW);
        drawGraph(graphSuhu,dataSuhu, seriesSuhu, Color.RED);
    }
    private void setGraph(GraphView graph){
        graph.getViewport().setYAxisBoundsManual(false);
//        graph.getViewport().setMinY(getMax());//nanti jadi min data dynamically
//        graph.getViewport().setMaxY(78500);//maks data dynamically, atau cek spesificaion

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(2.4);

        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph.getGridLabelRenderer().setNumHorizontalLabels(12);
        graph.getGridLabelRenderer().setLabelVerticalWidth(110);
        graph.getGridLabelRenderer().setGridColor(Color.RED);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().reloadStyles();
        graph.getGridLabelRenderer().setHorizontalAxisTitle("time (s)");

    }
    //DrawGraph Function
    private void drawGraph(GraphView graph, Integer[] data, LineGraphSeries<DataPoint> dataPoint, int colorGraph){
        dataPoint = new LineGraphSeries<>();
        double x,yData;

        Integer numDataPoint = (Integer) data.length;
        Log.i("test", Integer.toString(data.length));
        for(int i =0; i<numDataPoint; i++){
            System.out.println(i);
            x = (double) 0.02*i;
            yData = (double) data[i];
            dataPoint.appendData(new DataPoint(x,yData),true, numDataPoint);
//            yRekonstruksi = dataRekonstruksi[i];
//            yOriginal = dataOriginal[i];
//            ppgRekonstruksi.appendData(new DataPoint(x,yRekonstruksi), true, 100);
//            ppgOriginal.appendData(new DataPoint(x,yOriginal), true, 100);
        }
        graph.getViewport().setYAxisBoundsManual(false);
//        graph.getViewport().setMinY(getMax());//nanti jadi min data dynamically
//        graph.getViewport().setMaxY(78500);//maks data dynamically, atau cek spesificaion

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(2.4);

        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph.getGridLabelRenderer().setNumHorizontalLabels(12);
        graph.getGridLabelRenderer().setLabelVerticalWidth(110);
        graph.getGridLabelRenderer().setGridColor(Color.RED);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().reloadStyles();
        graph.getGridLabelRenderer().setHorizontalAxisTitle("time (s)");

        dataPoint.setColor(colorGraph);

        dataPoint.setTitle("TEST");
        graph.addSeries(dataPoint);

        //        ppgRekonstruksi.setColor(Color.RED);
//        ppgRekonstruksi.setTitle("Hasil Rekonstruksi");
//        graph.addSeries(ppgRekonstruksi);
//
//        ppgOriginal.setColor(Color.BLUE);
//        ppgOriginal.setTitle("Original");
//        graph.addSeries(ppgOriginal);

    }
    public void resetGraph(){
        //dataPPG
//        seriesEKG.resetData(DataPoint);
//        seriesEMG.resetData(DataPoint);
//        seriesAcceX.resetData(DataPoint);
//        seriesAcceY.resetData(new DataPoint[] {});
//        seriesAcceZ.resetData(new DataPoint[] {});
//        seriesSuhu.resetData(new DataPoint[] {});

        graphPPG.removeAllSeries();
        //EKG
        graphEKG.removeAllSeries();
        //EMG
        graphEMG.removeAllSeries();
        //Acce_X
        graphAcce.removeAllSeries();
        //EMG
        graphSuhu.removeAllSeries();
    }

    public void setLoggedInUser(){
        SharedPreferences sp1 = getActivity().getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        nameUser = sp1.getString("id_pasien", null);
        idUser = sp1.getString("id_pasien", null);
        Log.i("NAME USER IMAGE", nameUser);
        Log.i("ID USER IMAGE", idUser);
    }
}