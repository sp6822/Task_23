package com.example.task_23;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.task_23.databinding.FragmentSecondBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ArrayList<Expense> expenseList;
    private ArrayAdapter<Expense> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseList = new ArrayList<>();

        // הגדרת ה-Adapter המותאם אישית בתוך ה-ListView
        adapter = new ArrayAdapter<Expense>(requireContext(), R.layout.expense_list_item, expenseList) {
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

        binding.listViewExpenses.setAdapter(adapter);
        initFirebaseListener();
        binding.listViewExpenses.setOnItemLongClickListener((parent, view1, position, id) -> {
            Expense selectedExpense = expenseList.get(position);
            showActionDialog(selectedExpense);
            return true;
        });
    }

    private void initFirebaseListener() {
        FBref.myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();
                double totalSum = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    Expense expense = data.getValue(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense);
                        totalSum += expense.getAmount();
                    }
                }

                // מיונים וסינונים: המרת התאריך מ-String ל-Date ומיון מהחדש לישן
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                java.util.Collections.sort(expenseList, (e1, e2) -> {
                    try {
                        java.util.Date d1 = sdf.parse(e1.getDate());
                        java.util.Date d2 = sdf.parse(e2.getDate());
                        // מיון יורד (מהחדש ביותר לישן ביותר): e2 מושווה ל-e1
                        return d2.compareTo(d1);
                    } catch (Exception e) {
                        return 0; // במקרה של שגיאת פורמט, לא ישנה מיקום
                    }
                });

                adapter.notifyDataSetChanged();
                binding.tvTotalExpenses.setText("סך כל ההוצאות: " + totalSum + " ₪");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת הנתונים: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * מציג דיאלוג בחירה המאפשר למשתמש לבחור בין עדכון למחיקה של ההוצאה.
     */
    private void showActionDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("ניהול הוצאה: " + expense.getDescription())
                .setItems(new CharSequence[]{"עדכון סכום", "מחיקה"}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(expense);
                    } else if (which == 1) {
                        deleteExpense(expense);
                    }
                }).show();
    }
    private void showUpdateDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("עדכון סכום");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(expense.getAmount()));
        builder.setView(input);

        builder.setPositiveButton("אישור", (dialog, which) -> {
                    String newAmountStr = input.getText().toString().trim();
                    if (!newAmountStr.isEmpty()) {
                        double newAmount = Double.parseDouble(newAmountStr);
                        expense.setAmount(newAmount);

                        // עדכון הערך ב-Firebase לפי ה-Key הייחודי - קוד נקי ותקין!
                        FBref.myRef.child(expense.getKeyID()).setValue(expense)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(requireContext(), "הסכום עודכן!", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * מוחק את ההוצאה מ-Firebase.
     */
    private void deleteExpense(Expense expense) {
        FBref.myRef.child(expense.getKeyID()).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(requireContext(), "ההוצאה נמחקה בהצלחה!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}