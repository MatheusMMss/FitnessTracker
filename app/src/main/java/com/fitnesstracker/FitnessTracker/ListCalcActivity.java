package com.fitnesstracker;

import static androidx.core.os.LocaleListCompat.create;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListCalcActivity extends AppCompatActivity {

    private RecyclerView recycler_view_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calc);

        Bundle extras = getIntent().getExtras();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_list);

        if (extras != null) {
            String type = extras.getString("type");

            new Thread(() -> {
                List<Register> registers = SqlHelper.getInstance(this).getRegisterBy(type);

                runOnUiThread(() -> {
                    Log.d("Teste", registers.toString());
                    ListCalcAdapter adapter = new ListCalcAdapter(registers);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            }).start();
        }
    }

    private class ListCalcAdapter extends RecyclerView.Adapter<ListCalcViewHolder> implements OnAdapterItemClickListener {

        private List<Register> datas;

        public ListCalcAdapter(List<Register> datas) {
            this.datas = datas;
        }

        @NonNull
        @Override
        public ListCalcViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListCalcViewHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ListCalcViewHolder holder, int position) {
            Register data = datas.get(position);

            // listener oara ouvir evento de click e de long-click (segurar touch)
            holder.bind(data, this);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public void onClick(int id, String type) {
            // verificar qual tipo de dado deve ser EDITADO na tela seguinte
            switch (type) {
                case "imc":
                    Intent intent = new Intent(ListCalcActivity.this, ImcActivity.class);
                    intent.putExtra("updateId", id);
                    startActivity(intent);
                    break;
                case "tmb":
                    Intent i = new Intent(ListCalcActivity.this, TmbActivity.class);
                    i.putExtra("updateId", id);
                    startActivity(i);
                    break;
            }
        }

        @Override
        public void onLongClick(int position, String type, int id) {
            // evento de exclusão (PERGUNTAR ANTES PARA O USUARIO)
            AlertDialog alertDialog = new AlertDialog.Builder(ListCalcActivity.this)
                    .setMessage(getString(R.string.delete_message))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                        new Thread(() -> {
                            SqlHelper sqlHelper = SqlHelper.getInstance(ListCalcActivity.this);
                            long calcId = sqlHelper.removeItem(type, id);

                            runOnUiThread(() -> {
                                if (calcId > 0) {
                                    Toast.makeText(ListCalcActivity.this, R.string.removed, Toast.LENGTH_LONG).show();
                                    datas.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                        }).start();


                    })
                    .create();

            alertDialog.show();
        }
    }

    // Entenda como sendo a VIEW DA CELULA que está dentro da RecyclerView
    private class ListCalcViewHolder extends RecyclerView.ViewHolder {

        public ListCalcViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Register data, final OnAdapterItemClickListener onItemClickListener) {
            String formatted = "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("pt", "BR"));
                Date dateSaved = sdf.parse(data.createdDate);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
                formatted = dateFormat.format(dateSaved);
            } catch (ParseException e) {
            }

            ((TextView) itemView).setText(
                    getString(R.string.list_response, data.response, formatted)
            );

            //listener para ouvir evento de click (ABRIR EDIÇÃO)
            itemView.setOnClickListener(view -> {
                onItemClickListener.onClick(data.id, data.type);
            });

            //listener para ouvir evento de long-click (Segurar touch - EXCLUIR)
            itemView.setOnLongClickListener(view -> {
                onItemClickListener.onLongClick(getAdapterPosition(), data.type, data.id);
                return false;
            });
        }

    }
}