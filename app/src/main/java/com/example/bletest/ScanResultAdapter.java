package com.example.bletest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    ArrayList<ScanResultModel> results;

    public ScanResultAdapter(ArrayList<ScanResultModel> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = results.get(position).getDeviceName();
        String address = results.get(position).getMacAddress();
        int rssi = results.get(position).getSignalStrength();

        holder.setData(name, address, rssi);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView deviceName;
        private final TextView deviceAddress;
        private final TextView signalStrength;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceAddress = itemView.findViewById(R.id.macAddress);
            signalStrength = itemView.findViewById(R.id.signalStrength);
        }

        public void setData(String name, String address, int rssi) {
            deviceName.setText(name);
            deviceAddress.setText(address);
            signalStrength.setText(rssi);
        }
    }
}