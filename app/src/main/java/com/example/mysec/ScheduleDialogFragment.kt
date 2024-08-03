package com.example.mysec

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.DialogFragment
import com.example.mysec.databinding.DialogScheduleBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class ScheduleDialogFragment : DialogFragment() {

    private var _binding: DialogScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var scheduleDatabaseHelper: ScheduleDatabaseHelper

    // 콜백 인터페이스
    interface OnScheduleCreatedListener {
        fun onScheduleCreated(id: Int, title: String, dateRange: String, isOnline: Boolean, isEdit: Boolean)
    }

    private var listener: OnScheduleCreatedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        scheduleDatabaseHelper = ScheduleDatabaseHelper(context)
        // 콜백 인터페이스 구현체 가져오기
        listener = targetFragment as? OnScheduleCreatedListener
            ?: throw RuntimeException("$context must implement OnScheduleCreatedListener")
    }

    // 프래그먼트의 UI를 생성할 때 호출
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogScheduleBinding.inflate(inflater, container, false)

        // 인수에서 데이터 가져오기
        val title = arguments?.getString("title") ?: ""
        val dateRange = arguments?.getString("dateRange") ?: ""
        val isOnline = arguments?.getBoolean("isOnline") ?: false
        val isEdit = arguments?.getBoolean("isEdit") ?: false
        val id = arguments?.getInt("id") ?: -1

        // 버튼 텍스트 설정
        binding.btnCreate2.text = if (isEdit) "수정하기" else "추가하기"

        // 수정 모드일 경우 기존 데이터 설정
        if (isEdit) {
            if (isOnline) {
                binding.radioGroup.check(R.id.onBtn)
            } else {
                binding.radioGroup.check(R.id.offBtn)
            }

            binding.editTextInput2.setText(title)
            binding.dateRangeText2.text = dateRange
        } else {
            binding.radioGroup.check(R.id.offBtn)
        }

        // 날짜 선택 버튼 클릭 리스너
        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        // 생성 버튼 클릭 리스너
        binding.btnCreate2.setOnClickListener {
            validateAndCreateSchedule()
        }

        return binding.root
    }

    // 다이얼로그가 생성될 때 호출
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)
        return dialog
    }

    // 다이얼로그가 시작될 때 호출됨
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(850, 1100)
    }

    // 날짜 선택기를 표시하는 함수
    private fun showDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("프로젝트 기간 선택")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .build()
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val (startDateMillis, endDateMillis) = selection
            val startDate = Date(startDateMillis ?: 0L)
            val endDate = Date(endDateMillis ?: 0L)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateRangeText = "${dateFormat.format(startDate)} ~ ${dateFormat.format(endDate)}"

            binding.dateRangeText2.text = dateRangeText
            binding.dateRangeText2.setTextColor(resources.getColor(R.color.project_default, null))
        }

        dateRangePicker.show(parentFragmentManager, "dateRangePicker")
    }

    // 입력된 일정 정보를 검증하고 생성하거나 수정하는 함수
    private fun validateAndCreateSchedule() {
        val title = binding.editTextInput2.text.toString().trim()
        val dateRange = binding.dateRangeText2.text.toString().trim()
        val isOnline = binding.radioGroup.checkedRadioButtonId == R.id.onBtn
        val isEdit = arguments?.getBoolean("isEdit") ?: false
        val id = arguments?.getInt("id") ?: -1

        if (title.isNotEmpty() && dateRange.isNotEmpty()) {
            listener?.onScheduleCreated(id, title, dateRange, isOnline, isEdit)
            dismiss()
        } else {
            Toast.makeText(requireContext(), "내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // View가 파괴될 때 호출
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, dateRange: String, isOnline: Boolean, isEdit: Boolean, id: Int): ScheduleDialogFragment {
            val fragment = ScheduleDialogFragment()
            val args = Bundle().apply {
                putString("title", title)
                putString("dateRange", dateRange)
                putBoolean("isOnline", isOnline)
                putBoolean("isEdit", isEdit)
                putInt("id", id)
            }
            fragment.arguments = args
            return fragment
        }
    }
}