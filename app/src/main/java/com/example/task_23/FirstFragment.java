package com.example.task_23;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.task_23.databinding.FragmentFirstBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private final Calendar calendar = Calendar.getInstance();
    private final String[] categories = {"אוכל", "בילוי", "בריאות", "קניות", "אחר"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spCategory.setAdapter(adapter);

        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> saveExpenseToFirebase());
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            binding.etDate.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveExpenseToFirebase() {
        String description = binding.etDescription.getText().toString().trim();
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.spCategory.getSelectedItem().toString();
        String date = binding.etDate.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "אנא מלאי את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String keyID = FBref.myRef.push().getKey();

        if (keyID != null) {
            Expense newExpense = new Expense(keyID, description, amount, category, date);
            FBref.myRef.child(keyID).setValue(newExpense)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "ההוצאה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                        binding.etDescription.setText("");
                        binding.etAmount.setText("");
                        binding.etDate.setText("");
                        androidx.navigation.fragment.NavHostFragment.findNavController(FirstFragment.this)
                                .navigate(R.id.action_FirstFragment_to_SecondFragment);
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}