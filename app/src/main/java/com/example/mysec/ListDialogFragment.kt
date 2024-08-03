package com.example.mysec

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.DialogFragment
import com.example.mysec.databinding.DialogListBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_USER_ID = "user_id"
private const val TAG = "ListFragment"

class ListDialogFragment : DialogFragment() {

    // 바인딩 객체 저장
    private var _binding: DialogListBinding? = null
    private val binding get() = _binding!!
    private lateinit var projectRepository: ProjectRepository
    private var listener: OnProjectCreatedListener? = null

    private var userId: String? = null
    private lateinit var dbHelper: DBHelper

    // 호출 인터페이스
    interface OnProjectCreatedListener {
        fun onProjectCreated()
    }

    // 액티비티가 호출 인터페이스를 구현하는지 확인
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProjectCreatedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnProjectCreatedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            Log.d(TAG, "User ID: $userId")
        }
        dbHelper = DBHelper(requireContext())
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }

    // 프래그먼트 뷰 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogListBinding.inflate(inflater, container, false)
        projectRepository = ProjectRepository(requireContext())

        userId = userId ?: dbHelper.getUserId()

        // 날짜 선택 버튼 클릭 리스너 설정
        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        // 생성하기 버튼 클릭 리스너 설정
        binding.btnCreate.setOnClickListener {
            validateAndCreateProject()
        }

        return binding.root
    }

    // 다이얼로그 생성 및 배경 설정
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.background_shape)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(850, 900) // 다이얼로그 크기 설정
    }

    // 날짜 선택기 표시
    private fun showDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("프로젝트 기간 선택")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            // 선택된 날짜 범위 처리
            val (startDateMillis, endDateMillis) = selection

            // 날짜 밀리초 값을 날짜 객체로 변환
            val startDate = Date(startDateMillis ?: 0L)
            val endDate = Date(endDateMillis ?: 0L)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateRangeText = "${dateFormat.format(startDate)} ~ ${dateFormat.format(endDate)}"

            binding.dateRangeText.text = dateRangeText
            // 날짜 범위 텍스트 색상 변경
            binding.dateRangeText.setTextColor(resources.getColor(R.color.project_default, null))
        }

        dateRangePicker.show(parentFragmentManager, "dateRangePicker")
    }

    // 프로젝트 생성 유효성 검사 및 삽입
    private fun validateAndCreateProject() {
        val binding = _binding ?: return

        val projectName = binding.editTextInput.text.toString().trim()
        val projectDateRange = binding.dateRangeText.text.toString().trim()

        when {
            projectName.isEmpty() && projectDateRange.isEmpty() -> {
                showToast("프로젝트 제목과 프로젝트 기간을 설정하세요")
            }
            projectName.isEmpty() -> {
                showToast("프로젝트 제목을 입력하세요")
            }
            projectDateRange.isEmpty() || projectDateRange == "선택된 날짜 범위" -> {
                showToast("프로젝트 기간을 설정하세요")
            }
            else -> {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        if (userId.isNullOrEmpty()) {
                            showToast("사용자 아이디가 설정되지 않았습니다.")
                            return@launch
                        }

                        // 프로젝트 삽입
                        projectRepository.insertProject(projectName, projectDateRange, userId!!)
                        showToast("프로젝트가 생성되었습니다")
                        listener?.onProjectCreated()
                        dismiss()

                        // Activity를 통해 Fragment 표시
                        (requireActivity() as? MainActivity)?.showProjectFragment(
                            projectName, projectDateRange // 제목과 기간을 전달
                        )

                    } catch (e: Exception) {
                        showToast("프로젝트 생성 중 오류 발생: ${e.message}")
                    }
                }
            }
        }
    }

    // 토스트 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
