package com.example.task_23;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

// שונה ל-ThirdfragmentBinding בהתאם לקובץ ה-XML הנוכחי שלך
import com.example.task_23.databinding.ThirdfragmentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ThirdFragment extends Fragment {

    private ThirdfragmentBinding binding;
    private ArrayList<Expense> allExpensesList;
    private ArrayList<Expense> filteredList;
    private ArrayAdapter<Expense> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ThirdfragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allExpensesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // תיקון מערך הקטגוריות להתאמה מלאה ל-FirstFragment
        String[] categories = {"כל הקטגוריות", "אוכל", "בילוי", "בריאות", "קניות", "אחר"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFilterCategory.setAdapter(spinnerAdapter);

        // שדרוג ה-Adapter לתצוגה מעוצבת עם ה-Layout המותאם אישית שלך
        adapter = new ArrayAdapter<Expense>(requireContext(), R.layout.expense_list_item, filteredList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.expense_list_item, parent, false);
                }

                Expense expense = getItem(position);

                android.widget.TextView tvDesc = convertView.findViewById(R.id.tvItemDescription);
                android.widget.TextView tvDetails = convertView.findViewById(R.id.tvItemDetails);
                android.widget.TextView tvAmount = convertView.findViewById(R.id.tvItemAmount);

                if (expense != null) {
                    tvDesc.setText(expense.getDescription());
                    tvDetails.setText(expense.getCategory() + " | " + expense.getDate());
                    tvAmount.setText(expense.getAmount() + " ₪");
                }

                return convertView;
            }
        };

        binding.lvFilterResults.setAdapter(adapter);

        loadExpensesFromFirebase();
        binding.btnApplyFilter.setOnClickListener(v -> applyFilters());
    }

    private void loadExpensesFromFirebase() {
        FBref.myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allExpensesList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Expense expense = data.getValue(Expense.class);
                    if (expense != null) {
                        allExpensesList.add(expense);
                    }
                }
                filteredList.clear();
                filteredList.addAll(allExpensesList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String searchDesc = binding.etSearchDescription.getText().toString().trim().toLowerCase();
        String selectedCategory = binding.spFilterCategory.getSelectedItem().toString();
        String minAmountStr = binding.etMinAmount.getText().toString().trim();

        double minAmount = 0;
        if (!minAmountStr.isEmpty()) {
            try {
                minAmount = Double.parseDouble(minAmountStr);
            } catch (NumberFormatException e) {
                minAmount = 0;
            }
        }

        filteredList.clear();

        for (Expense expense : allExpensesList) {
            boolean matchesDesc = expense.getDescription().toLowerCase().contains(searchDesc);
            boolean matchesCategory = selectedCategory.equals("כל הקטגוריות") || expense.getCategory().equals(selectedCategory);
            boolean matchesAmount = expense.getAmount() >= minAmount;

            if (matchesDesc && matchesCategory && matchesAmount) {
                filteredList.add(expense);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "לא נמצאו הוצאות המתאימות לסינון", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}