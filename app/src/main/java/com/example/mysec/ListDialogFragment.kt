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
import com.example.mysec.databinding.ListdialogBackgroundBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ListDialogFragment : DialogFragment() {

    // 바인딩 객체 저장
    private var _binding: ListdialogBackgroundBinding? = null
    // 바인딩 객체에 접근
    private val binding get() = _binding!!
    // 프로젝트 저장을 위한 Repository 인스턴스
    private lateinit var projectRepository: ProjectRepository
    private var listener: OnProjectCreatedListener? = null

    // 호출 인터페이스
    interface OnProjectCreatedListener {
        fun onProjectCreated()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 액티비티가 호출 인터페이스를 구현하는지 확인
        listener = context as? OnProjectCreatedListener
    }

    // 프래그먼트 뷰 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View 바인딩 초기화
        _binding = ListdialogBackgroundBinding.inflate(inflater, container, false)
        // Repository 초기화
        projectRepository = ProjectRepository(requireContext())

        // 날짜 선택 버튼 클릭 리스너 설정
        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        // 생성하기 버튼 클릭 리스너 설정
        binding.btnCreate.setOnClickListener {
            validateAndCreateProject()
        }

        // 생성된 뷰 반환
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // DialogFragment의 기본 다이얼로그를 생성
        val dialog = super.onCreateDialog(savedInstanceState)

        // 다이얼로그의 배경 설정
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)

        return dialog
    }

    override fun onStart() {
        super.onStart()

        // 다이얼로그 크기 설정
        dialog?.window?.setLayout(850, 900)
    }

    private fun showDatePicker() {
        // 시작일과 종료일을 선택할 수 있는 날짜 선택기 생성
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("프로젝트 기간 선택")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            // 선택된 날짜 범위 처리
            val (startDateMillis, endDateMillis) = selection
            val startDate = Date(startDateMillis ?: 0L)
            val endDate = Date(endDateMillis ?: 0L)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateRangeText = "${dateFormat.format(startDate)} ~ ${dateFormat.format(endDate)}"

            binding.dateRangeText.text = dateRangeText
            // 날짜 범위 텍스트 색상 변경
            binding.dateRangeText.setTextColor(resources.getColor(R.color.project_deafult, null))
        }

        dateRangePicker.show(parentFragmentManager, "dateRangePicker")
    }

    // 프로젝트 제목이나 프로젝트 기간 미입력 시 토스트 메시지 출력
    private fun validateAndCreateProject() {
        // 프로젝트 제목
        val projectName = binding.editTextInput.text.toString().trim()
        // 프로젝트 기간
        val projectDateRange = binding.dateRangeText.text.toString().trim()

        // 1. 프로젝트 제목 작성 X, 프로젝트 기간 설정 X
        if (projectName.isEmpty() && projectDateRange.isEmpty()) {
            showToast("프로젝트 제목과 프로젝트 기간을 설정하세요")
        } // 2. 프로젝트 제목 작성 X
        else if (projectName.isEmpty()) {
            showToast("프로젝트 제목을 입력하세요")
        } // 3. 프로젝트 기간 설정 X
        else if (projectDateRange.isEmpty() || projectDateRange == "선택된 날짜 범위") {
            showToast("프로젝트 기간을 설정하세요")
        } // 4. 프로젝트 제목 작성 O, 프로젝트 기간 설정 O
        else {
            // CoroutineScope를 사용하여 데이터베이스에 삽입
            CoroutineScope(Dispatchers.Main).launch {
                projectRepository.insertProject(projectName, projectDateRange)
                showToast("프로젝트가 생성되었습니다")
                listener?.onProjectCreated()
                dismiss()
                showProjectFragment(projectName.take(2))
            }
        }
    }

    private fun showProjectFragment(projectInitials: String) {
        val fragment = ProjectFragment().apply {
            arguments = Bundle().apply {
                putString("project_initials", projectInitials)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}