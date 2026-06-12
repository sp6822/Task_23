package com.example.task_23;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.task_23.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // מאזין לכפתור שמירת ההוצאה
        binding.btnSaveExpense.setOnClickListener(v -> saveExpenseToFirebase());

        // כפתור אופציונלי למעבר ישיר למסך רשימת ההוצאות (בלי לשמור)
        // אם אין לך כפתור כזה ב-XML, את יכולה למחוק את 3 השורות הבאות או להוסיף לו ID תואם
        if (binding.btnViewExpenses != null) {
            binding.btnViewExpenses.setOnClickListener(v ->
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment)
            );
        }
    }

    private void saveExpenseToFirebase() {
        // 1. שליפת הנתונים מכל הרכיבים במסך
        String desc = binding.etDescription.getText().toString().trim();
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String date = binding.etDate.getText().toString().trim();

        // 2. בדיקת תקינות קלט (הגנה מפני קריסות במקרה של שדות ריקים)
        if (desc.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "בבקשה מלאי את כל השדות לפני השמירה!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "הסכום שהוזן אינו מספר תקין!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. יצירת מפתח ייחודי בתוך ה-Database
        String key = FBref.myRef.push().getKey();
        if (key == null) {
            Toast.makeText(requireContext(), "שגיאה: לא ניתן ליצור מפתח בסיס נתונים", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. בניית אובייקט ההוצאה החדש
        Expense newExpense = new Expense(key, desc, amount, category, date);

        // 5. שמירה ב-Firebase באמצעות מאזין הצלחה חד-פעמי (addOnSuccessListener)
        // זהו המפתח למניעת הקריסה בשמירה השנייה!
        FBref.myRef.child(key).setValue(newExpense)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "ההוצאה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();

                    // איפוס השדות כדי שהמסך יהיה נקי לפעם הבאה
                    binding.etDescription.setText("");
                    binding.etAmount.setText("");
                    binding.etDate.setText("");

                    // ניווט בטוח למסך השני רק לאחר שהשמירה הסתיימה בהצלחה
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                })
                .addOnFailureListener(e -> {
                    // במקרה של תקלה ברשת או בשרת של Firebase
                    Toast.makeText(requireContext(), "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // מניעת זליגת זיכרון (Memory Leak) כמקובל ב-Fragments
    }
}