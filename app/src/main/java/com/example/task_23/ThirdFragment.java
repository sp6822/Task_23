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
        // שימוש ב-FragmentThirdBinding לפי הדרישה
        binding = ThirdfragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // תיקון הקריאה ל-super
        super.onViewCreated(view, savedInstanceState);

        allExpensesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // 1. הגדרת ה-Spinner עם קטגוריות
        String[] categories = {"כל הקטגוריות", "אוכל", "קניות", "תחבורה", "בריאות", "אחר"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFilterCategory.setAdapter(spinnerAdapter);

        // 2. הגדרת ה-Adapter עבור ה-ListView של התוצאות
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, filteredList);
        binding.lvFilterResults.setAdapter(adapter);

        // 3. משיכת הנתונים מ-Firebase
        loadExpensesFromFirebase();

        // 4. האזנה לכפתור הסינון
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
                // בהתחלה מציגים את כל הרשימה
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
            // סינון 1: חיפוש לפי תיאור
            boolean matchesDesc = expense.getDescription().toLowerCase().contains(searchDesc);

            // סינון 2: חיפוש לפי קטגוריה
            boolean matchesCategory = selectedCategory.equals("כל הקטגוריות") || expense.getCategory().equals(selectedCategory);

            // סינון 3: חיפוש לפי סכום מינימלי
            boolean matchesAmount = expense.getAmount() >= minAmount;

            // שילוב כל התנאים יחד
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